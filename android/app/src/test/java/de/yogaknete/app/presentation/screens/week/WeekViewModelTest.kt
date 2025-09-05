package de.yogaknete.app.presentation.screens.week

import de.yogaknete.app.data.local.YogaClassDao
import de.yogaknete.app.domain.model.ClassStatus
import de.yogaknete.app.domain.model.Studio
import de.yogaknete.app.domain.model.YogaClass
import de.yogaknete.app.domain.repository.StudioRepository
import de.yogaknete.app.domain.repository.ClassTemplateRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import kotlinx.datetime.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for WeekViewModel - the week view business logic
 */
@ExperimentalCoroutinesApi
class WeekViewModelTest {
    
    private lateinit var yogaClassDao: YogaClassDao
    private lateinit var studioRepository: StudioRepository
    private lateinit var classTemplateRepository: ClassTemplateRepository
    private lateinit var viewModel: WeekViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        yogaClassDao = mockk()
        studioRepository = mockk()
        classTemplateRepository = mockk()
        
        // Default mocks
        every { yogaClassDao.getClassesInRange(any(), any()) } returns flowOf(emptyList())
        every { studioRepository.getAllActiveStudios() } returns flowOf(emptyList())
        every { classTemplateRepository.getAllActiveTemplates() } returns flowOf(emptyList())
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `initial state has current week dates`() = runTest {
        viewModel = WeekViewModel(yogaClassDao, studioRepository, classTemplateRepository)
        
        val state = viewModel.state.value
        
        // Should have 7 days in the week
        assertEquals(7, state.weekDays.size)
        
        // First day should be Monday
        assertEquals(DayOfWeek.MONDAY, state.weekDays.first().dayOfWeek)
        
        // Last day should be Sunday
        assertEquals(DayOfWeek.SUNDAY, state.weekDays.last().dayOfWeek)
    }
    
    @Test
    fun `navigateToPreviousWeek moves back 7 days`() = runTest {
        viewModel = WeekViewModel(yogaClassDao, studioRepository, classTemplateRepository)
        val initialStart = viewModel.state.value.currentWeekStart
        
        viewModel.navigateToPreviousWeek()
        advanceUntilIdle()
        
        val newStart = viewModel.state.value.currentWeekStart
        
        // Should be 7 days earlier
        val daysDifference = initialStart.toEpochDays() - newStart.toEpochDays()
        assertEquals(7, daysDifference)
    }
    
    @Test
    fun `navigateToNextWeek moves forward 7 days`() = runTest {
        viewModel = WeekViewModel(yogaClassDao, studioRepository, classTemplateRepository)
        val initialStart = viewModel.state.value.currentWeekStart
        
        viewModel.navigateToNextWeek()
        advanceUntilIdle()
        
        val newStart = viewModel.state.value.currentWeekStart
        
        // Should be 7 days later
        val daysDifference = newStart.toEpochDays() - initialStart.toEpochDays()
        assertEquals(7, daysDifference)
    }
    
    @Test
    fun `addClass creates yoga class with correct data`() = runTest {
        coEvery { yogaClassDao.insertClass(any()) } returns 1L
        
        viewModel = WeekViewModel(yogaClassDao, studioRepository, classTemplateRepository)
        
        viewModel.addClass(
            studioId = 1L,
            title = "Hatha Yoga",
            date = LocalDate(2024, 11, 4),
            startHour = 17,
            startMinute = 30,
            endHour = 18,
            endMinute = 45
        )
        
        advanceUntilIdle()
        
        // Verify class was inserted with correct properties
        coVerify {
            yogaClassDao.insertClass(
                match { yogaClass ->
                    yogaClass.studioId == 1L &&
                    yogaClass.title == "Hatha Yoga" &&
                    yogaClass.durationHours == 1.25 &&
                    yogaClass.status == ClassStatus.SCHEDULED
                }
            )
        }
    }
    
    @Test
    fun `markClassAsCompleted updates status`() = runTest {
        coEvery { yogaClassDao.updateClassStatus(any(), any()) } just Runs
        
        viewModel = WeekViewModel(yogaClassDao, studioRepository, classTemplateRepository)
        
        val testClass = YogaClass(
            id = 42L,
            studioId = 1L,
            title = "Test Yoga",
            startTime = LocalDateTime(2024, 11, 4, 17, 30),
            endTime = LocalDateTime(2024, 11, 4, 18, 45),
            durationHours = 1.25
        )
        
        viewModel.markClassAsCompleted(testClass)
        advanceUntilIdle()
        
        coVerify {
            yogaClassDao.updateClassStatus(42L, ClassStatus.COMPLETED)
        }
    }
    
    @Test
    fun `state calculates totals correctly`() = runTest {
        val classes = listOf(
            YogaClass(
                id = 1,
                studioId = 1,
                title = "Class 1",
                startTime = LocalDateTime(2024, 11, 4, 17, 30),
                endTime = LocalDateTime(2024, 11, 4, 18, 45),
                durationHours = 1.25,
                status = ClassStatus.COMPLETED
            ),
            YogaClass(
                id = 2,
                studioId = 1,
                title = "Class 2",
                startTime = LocalDateTime(2024, 11, 5, 19, 0),
                endTime = LocalDateTime(2024, 11, 5, 20, 15),
                durationHours = 1.25,
                status = ClassStatus.SCHEDULED
            ),
            YogaClass(
                id = 3,
                studioId = 1,
                title = "Class 3",
                startTime = LocalDateTime(2024, 11, 6, 18, 0),
                endTime = LocalDateTime(2024, 11, 6, 19, 15),
                durationHours = 1.25,
                status = ClassStatus.CANCELLED // Should not count
            )
        )
        
        every { yogaClassDao.getClassesInRange(any(), any()) } returns flowOf(classes)
        
        viewModel = WeekViewModel(yogaClassDao, studioRepository, classTemplateRepository)
        advanceUntilIdle()
        
        val state = viewModel.state.value
        
        // Only 2 non-cancelled classes
        assertEquals(2, state.totalClassesThisWeek)
        
        // Total hours (2 * 1.25)
        assertEquals(2.5, state.totalHoursThisWeek, 0.01)
    }
}
