package de.yogaknete.app.domain.usecase

import de.yogaknete.app.data.local.YogaClassDao
import de.yogaknete.app.data.local.dao.ClassTemplateDao
import de.yogaknete.app.data.local.entities.ClassTemplate
import de.yogaknete.app.domain.model.YogaClass
import de.yogaknete.app.domain.service.ClassNotificationScheduler
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.*
import org.junit.After
import org.junit.Before
import org.junit.Test

class AutoScheduleManagerTest {

    private lateinit var classTemplateDao: ClassTemplateDao
    private lateinit var yogaClassDao: YogaClassDao
    private lateinit var notificationScheduler: ClassNotificationScheduler
    private lateinit var autoScheduleManager: AutoScheduleManager

    @Before
    fun setup() {
        classTemplateDao = mockk()
        yogaClassDao = mockk()
        notificationScheduler = mockk(relaxed = true)
        autoScheduleManager = AutoScheduleManager(classTemplateDao, yogaClassDao, notificationScheduler)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    private fun mockToday(date: LocalDate) {
        mockkStatic(Clock.System::todayIn)
        every { Clock.System.todayIn(any()) } returns date
    }

    private fun createTemplate(
        id: Long = 1L,
        dayOfWeek: DayOfWeek = DayOfWeek.WEDNESDAY,
        startTime: LocalTime = LocalTime(10, 0),
        endTime: LocalTime = LocalTime(11, 15),
        studioId: Long = 1L,
        autoSchedule: Boolean = true,
        isActive: Boolean = true,
        lastScheduledDate: LocalDate? = null,
        recurrenceIntervalWeeks: Int = 1,
        referenceDate: LocalDate? = null
    ) = ClassTemplate(
        id = id,
        name = "Test Template",
        studioId = studioId,
        className = "Hatha Yoga",
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        endTime = endTime,
        duration = 1.25,
        isActive = isActive,
        autoSchedule = autoSchedule,
        lastScheduledDate = lastScheduledDate,
        recurrenceIntervalWeeks = recurrenceIntervalWeeks,
        referenceDate = referenceDate
    )

    @Test
    fun `catchUpAutoSchedule creates classes for active auto-schedule templates`() = runTest {
        // Given: today is Wednesday 2025-01-08
        val today = LocalDate(2025, 1, 8)
        mockToday(today)

        val template = createTemplate(dayOfWeek = DayOfWeek.FRIDAY)

        coEvery { classTemplateDao.getAutoScheduleTemplates() } returns listOf(template)
        coEvery { yogaClassDao.getClassAtTime(any(), any()) } returns null
        coEvery { yogaClassDao.insertClass(any()) } returns 1L
        coEvery { classTemplateDao.updateLastScheduledDate(any(), any()) } just Runs

        // When
        autoScheduleManager.catchUpAutoSchedule()

        // Then: a class should be created for Friday 2025-01-10
        coVerify {
            yogaClassDao.insertClass(match<YogaClass> {
                it.studioId == 1L &&
                    it.title == "Hatha Yoga" &&
                    it.startTime == LocalDateTime(2025, 1, 10, 10, 0)
            })
        }
        coVerify { classTemplateDao.updateLastScheduledDate(1L, LocalDate(2025, 1, 10)) }
    }

    @Test
    fun `catchUpAutoSchedule does not create classes when autoSchedule is false`() = runTest {
        // Given: getAutoScheduleTemplates only returns templates with autoSchedule=true
        val today = LocalDate(2025, 1, 8)
        mockToday(today)

        // The DAO query filters for autoSchedule=true, so an empty list is returned
        coEvery { classTemplateDao.getAutoScheduleTemplates() } returns emptyList()

        // When
        autoScheduleManager.catchUpAutoSchedule()

        // Then: no classes should be created
        coVerify(exactly = 0) { yogaClassDao.insertClass(any()) }
    }

    @Test
    fun `catchUpAutoSchedule does not create duplicate classes`() = runTest {
        // Given: today is Wednesday 2025-01-08, existing class at same time/studio
        val today = LocalDate(2025, 1, 8)
        mockToday(today)

        val template = createTemplate(dayOfWeek = DayOfWeek.FRIDAY, studioId = 1L)
        val existingClass = YogaClass(
            id = 99L,
            studioId = 1L,
            title = "Hatha Yoga",
            startTime = LocalDateTime(2025, 1, 10, 10, 0),
            endTime = LocalDateTime(2025, 1, 10, 11, 15),
            durationHours = 1.25
        )

        coEvery { classTemplateDao.getAutoScheduleTemplates() } returns listOf(template)
        coEvery {
            yogaClassDao.getClassAtTime(LocalDateTime(2025, 1, 10, 10, 0), 1L)
        } returns existingClass

        // When
        autoScheduleManager.catchUpAutoSchedule()

        // Then: no new class should be created
        coVerify(exactly = 0) { yogaClassDao.insertClass(any()) }
    }

    @Test
    fun `catchUpAutoSchedule does not schedule in the past`() = runTest {
        // Given: today is Thursday 2025-01-09, template is for Monday (2025-01-06 = past)
        val today = LocalDate(2025, 1, 9)
        mockToday(today)

        val template = createTemplate(dayOfWeek = DayOfWeek.MONDAY)

        coEvery { classTemplateDao.getAutoScheduleTemplates() } returns listOf(template)
        // Thursday, Friday of current week are future, but Monday is past
        coEvery { yogaClassDao.getClassAtTime(any(), any()) } returns null
        coEvery { yogaClassDao.insertClass(any()) } returns 1L
        coEvery { classTemplateDao.updateLastScheduledDate(any(), any()) } just Runs

        // When
        autoScheduleManager.catchUpAutoSchedule()

        // Then: no class on Monday 2025-01-06 (past)
        coVerify(exactly = 0) {
            yogaClassDao.insertClass(match<YogaClass> {
                it.startTime.date == LocalDate(2025, 1, 6)
            })
        }
    }

    @Test
    fun `catchUpAutoSchedule does not schedule beyond 7-day window`() = runTest {
        // Given: today is Monday 2025-01-06, template is for Sunday
        // Current week Sunday = 2025-01-12, next week Sunday = 2025-01-19
        // 7 days from today = 2025-01-13, so 2025-01-19 is beyond window
        val today = LocalDate(2025, 1, 6)
        mockToday(today)

        val template = createTemplate(dayOfWeek = DayOfWeek.SUNDAY)

        coEvery { classTemplateDao.getAutoScheduleTemplates() } returns listOf(template)
        coEvery { yogaClassDao.getClassAtTime(any(), any()) } returns null
        coEvery { yogaClassDao.insertClass(any()) } returns 1L
        coEvery { classTemplateDao.updateLastScheduledDate(any(), any()) } just Runs

        // When
        autoScheduleManager.catchUpAutoSchedule()

        // Then: class created for 2025-01-12 (within window) but not 2025-01-19
        coVerify {
            yogaClassDao.insertClass(match<YogaClass> {
                it.startTime.date == LocalDate(2025, 1, 12)
            })
        }
        coVerify(exactly = 0) {
            yogaClassDao.insertClass(match<YogaClass> {
                it.startTime.date == LocalDate(2025, 1, 19)
            })
        }
    }

    @Test
    fun `catchUpAutoSchedule updates lastScheduledDate after scheduling`() = runTest {
        // Given: today is Wednesday 2025-01-08
        val today = LocalDate(2025, 1, 8)
        mockToday(today)

        val template = createTemplate(id = 5L, dayOfWeek = DayOfWeek.THURSDAY)

        coEvery { classTemplateDao.getAutoScheduleTemplates() } returns listOf(template)
        coEvery { yogaClassDao.getClassAtTime(any(), any()) } returns null
        coEvery { yogaClassDao.insertClass(any()) } returns 1L
        coEvery { classTemplateDao.updateLastScheduledDate(any(), any()) } just Runs

        // When
        autoScheduleManager.catchUpAutoSchedule()

        // Then: lastScheduledDate updated for Thursday 2025-01-09
        coVerify { classTemplateDao.updateLastScheduledDate(5L, LocalDate(2025, 1, 9)) }
    }

    @Test
    fun `catchUpAutoSchedule bi-weekly schedules on matching weeks`() = runTest {
        // Given: today is Wednesday 2025-01-08, template for Wednesday bi-weekly
        // referenceDate = 2025-01-08 (today, a Wednesday) -> should schedule today
        val today = LocalDate(2025, 1, 8)
        mockToday(today)

        val template = createTemplate(
            dayOfWeek = DayOfWeek.WEDNESDAY,
            recurrenceIntervalWeeks = 2,
            referenceDate = LocalDate(2025, 1, 8)
        )

        coEvery { classTemplateDao.getAutoScheduleTemplates() } returns listOf(template)
        coEvery { yogaClassDao.getClassAtTime(any(), any()) } returns null
        coEvery { yogaClassDao.insertClass(any()) } returns 1L
        coEvery { classTemplateDao.updateLastScheduledDate(any(), any()) } just Runs

        // When
        autoScheduleManager.catchUpAutoSchedule()

        // Then: class created for 2025-01-08 (on-week)
        coVerify {
            yogaClassDao.insertClass(match<YogaClass> {
                it.startTime.date == LocalDate(2025, 1, 8)
            })
        }
    }

    @Test
    fun `catchUpAutoSchedule bi-weekly skips off-weeks`() = runTest {
        // Given: today is Wednesday 2025-01-15, template for Wednesday bi-weekly
        // referenceDate = 2025-01-08 -> next on-week would be 2025-01-22
        // 2025-01-15 is 1 week from ref, so off-week
        val today = LocalDate(2025, 1, 15)
        mockToday(today)

        val template = createTemplate(
            dayOfWeek = DayOfWeek.WEDNESDAY,
            recurrenceIntervalWeeks = 2,
            referenceDate = LocalDate(2025, 1, 8)
        )

        coEvery { classTemplateDao.getAutoScheduleTemplates() } returns listOf(template)
        coEvery { yogaClassDao.getClassAtTime(any(), any()) } returns null
        coEvery { yogaClassDao.insertClass(any()) } returns 1L
        coEvery { classTemplateDao.updateLastScheduledDate(any(), any()) } just Runs

        // When
        autoScheduleManager.catchUpAutoSchedule()

        // Then: no class on 2025-01-15 (off-week), class on 2025-01-22 (on-week, within 14-day window)
        coVerify(exactly = 0) {
            yogaClassDao.insertClass(match<YogaClass> {
                it.startTime.date == LocalDate(2025, 1, 15)
            })
        }
        coVerify {
            yogaClassDao.insertClass(match<YogaClass> {
                it.startTime.date == LocalDate(2025, 1, 22)
            })
        }
    }

    @Test
    fun `catchUpAutoSchedule 4-weekly scheduling works correctly`() = runTest {
        // Given: today is Wednesday 2025-01-08, template for Wednesday every 4 weeks
        // referenceDate = 2025-01-08 -> next on-week = 2025-02-05 (4 weeks later)
        val today = LocalDate(2025, 1, 8)
        mockToday(today)

        val template = createTemplate(
            dayOfWeek = DayOfWeek.WEDNESDAY,
            recurrenceIntervalWeeks = 4,
            referenceDate = LocalDate(2025, 1, 8)
        )

        coEvery { classTemplateDao.getAutoScheduleTemplates() } returns listOf(template)
        coEvery { yogaClassDao.getClassAtTime(any(), any()) } returns null
        coEvery { yogaClassDao.insertClass(any()) } returns 1L
        coEvery { classTemplateDao.updateLastScheduledDate(any(), any()) } just Runs

        // When
        autoScheduleManager.catchUpAutoSchedule()

        // Then: class created for 2025-01-08 (on-week), not for 2025-01-15/22/29 (off-weeks)
        coVerify {
            yogaClassDao.insertClass(match<YogaClass> {
                it.startTime.date == LocalDate(2025, 1, 8)
            })
        }
        coVerify(exactly = 0) {
            yogaClassDao.insertClass(match<YogaClass> {
                it.startTime.date == LocalDate(2025, 1, 15)
            })
        }
        coVerify(exactly = 0) {
            yogaClassDao.insertClass(match<YogaClass> {
                it.startTime.date == LocalDate(2025, 1, 22)
            })
        }
        coVerify(exactly = 0) {
            yogaClassDao.insertClass(match<YogaClass> {
                it.startTime.date == LocalDate(2025, 1, 29)
            })
        }
    }

    @Test
    fun `catchUpAutoSchedule null referenceDate treats as weekly`() = runTest {
        // Given: bi-weekly template with null referenceDate -> should behave as weekly
        val today = LocalDate(2025, 1, 8)
        mockToday(today)

        val template = createTemplate(
            dayOfWeek = DayOfWeek.WEDNESDAY,
            recurrenceIntervalWeeks = 2,
            referenceDate = null
        )

        coEvery { classTemplateDao.getAutoScheduleTemplates() } returns listOf(template)
        coEvery { yogaClassDao.getClassAtTime(any(), any()) } returns null
        coEvery { yogaClassDao.insertClass(any()) } returns 1L
        coEvery { classTemplateDao.updateLastScheduledDate(any(), any()) } just Runs

        // When
        autoScheduleManager.catchUpAutoSchedule()

        // Then: class created for today (null referenceDate -> isOnRecurrenceWeek returns true)
        coVerify {
            yogaClassDao.insertClass(match<YogaClass> {
                it.startTime.date == LocalDate(2025, 1, 8)
            })
        }
    }

    @Test
    fun `catchUpAutoSchedule 4-weekly uses 28-day advance window`() = runTest {
        // Given: today is Monday 2025-01-06, template for Monday every 4 weeks
        // advanceDays = max(7, 4*7) = 28
        // referenceDate = 2025-01-06 -> next on-week = 2025-02-03
        // 2025-02-03 is 28 days from 2025-01-06, should be within window
        val today = LocalDate(2025, 1, 6)
        mockToday(today)

        val template = createTemplate(
            dayOfWeek = DayOfWeek.MONDAY,
            recurrenceIntervalWeeks = 4,
            referenceDate = LocalDate(2025, 1, 6)
        )

        coEvery { classTemplateDao.getAutoScheduleTemplates() } returns listOf(template)
        coEvery { yogaClassDao.getClassAtTime(any(), any()) } returns null
        coEvery { yogaClassDao.insertClass(any()) } returns 1L
        coEvery { classTemplateDao.updateLastScheduledDate(any(), any()) } just Runs

        // When
        autoScheduleManager.catchUpAutoSchedule()

        // Then: class created for 2025-01-06 and 2025-02-03 (28 days = within window)
        coVerify {
            yogaClassDao.insertClass(match<YogaClass> {
                it.startTime.date == LocalDate(2025, 1, 6)
            })
        }
        coVerify {
            yogaClassDao.insertClass(match<YogaClass> {
                it.startTime.date == LocalDate(2025, 2, 3)
            })
        }
    }

    @Test
    fun `catchUpAutoSchedule schedules notification for created class`() = runTest {
        val today = LocalDate(2025, 1, 8)
        mockToday(today)

        val template = createTemplate(dayOfWeek = DayOfWeek.FRIDAY)

        coEvery { classTemplateDao.getAutoScheduleTemplates() } returns listOf(template)
        coEvery { yogaClassDao.getClassAtTime(any(), any()) } returns null
        coEvery { yogaClassDao.insertClass(any()) } returns 7L
        coEvery { classTemplateDao.updateLastScheduledDate(any(), any()) } just Runs

        autoScheduleManager.catchUpAutoSchedule()

        verify { notificationScheduler.schedule(match { it.id == 7L }) }
    }
}
