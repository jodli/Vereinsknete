package de.yogaknete.app.presentation.screens.week

import de.yogaknete.app.data.local.YogaClassDao
import de.yogaknete.app.data.local.entities.ClassTemplate
import de.yogaknete.app.domain.model.ClassStatus
import de.yogaknete.app.domain.model.CreationSource
import de.yogaknete.app.domain.model.YogaClass
import de.yogaknete.app.domain.repository.ClassTemplateRepository
import de.yogaknete.app.domain.repository.StudioRepository
import de.yogaknete.app.domain.usecase.AutoScheduleManager
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeekViewModelTemplateTest {

    private lateinit var yogaClassDao: YogaClassDao
    private lateinit var studioRepository: StudioRepository
    private lateinit var classTemplateRepository: ClassTemplateRepository
    private lateinit var autoScheduleManager: AutoScheduleManager
    private lateinit var viewModel: WeekViewModel
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        yogaClassDao = mockk()
        studioRepository = mockk()
        classTemplateRepository = mockk()
        autoScheduleManager = mockk(relaxed = true)

        every { yogaClassDao.getClassesInRange(any(), any()) } returns flowOf(emptyList())
        every { studioRepository.getAllActiveStudios() } returns flowOf(emptyList())
        every { classTemplateRepository.getAllActiveTemplates() } returns flowOf(emptyList())
        coEvery { yogaClassDao.insertClass(any()) } returns 1L

        viewModel = WeekViewModel(yogaClassDao, studioRepository, classTemplateRepository, autoScheduleManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `createClassFromTemplate inserts class with expected values`() = runTest {
        val template = ClassTemplate(
            id = 10L,
            name = "Montag Morgen",
            studioId = 5L,
            className = "Vinyasa Flow",
            dayOfWeek = DayOfWeek.MONDAY,
            startTime = LocalTime(9, 0),
            endTime = LocalTime(10, 15),
            duration = 1.25,
            isActive = true
        )
        val date = LocalDate(2024, 11, 4) // Monday

        viewModel.createClassFromTemplate(template, date)
        advanceUntilIdle()

        coVerify {
            yogaClassDao.insertClass(match<YogaClass> {
                it.studioId == 5L &&
                it.title == "Vinyasa Flow" &&
                it.durationHours == 1.25 &&
                it.status == ClassStatus.SCHEDULED &&
                it.creationSource == CreationSource.TEMPLATE &&
                it.sourceTemplateId == 10L &&
                it.startTime.hour == 9 && it.startTime.minute == 0 &&
                it.endTime.hour == 10 && it.endTime.minute == 15
            })
        }
    }
}

