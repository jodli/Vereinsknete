package de.yogaknete.app.presentation.screens.templates

import de.yogaknete.app.data.local.entities.ClassTemplate
import de.yogaknete.app.domain.repository.ClassTemplateRepository
import de.yogaknete.app.domain.repository.StudioRepository
import de.yogaknete.app.domain.repository.YogaClassRepository
import de.yogaknete.app.domain.usecase.AutoScheduleManager
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
import kotlinx.datetime.LocalTime
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TemplateViewModelTest {

    private lateinit var classTemplateRepository: ClassTemplateRepository
    private lateinit var studioRepository: StudioRepository
    private lateinit var yogaClassRepository: YogaClassRepository
    private lateinit var autoScheduleManager: AutoScheduleManager
    private lateinit var viewModel: TemplateViewModel
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        classTemplateRepository = mockk()
        studioRepository = mockk()
        yogaClassRepository = mockk()
        autoScheduleManager = mockk(relaxed = true)

        every { classTemplateRepository.getAllTemplates() } returns flowOf(emptyList())
        every { studioRepository.getAllActiveStudios() } returns flowOf(emptyList())
        coEvery { classTemplateRepository.createTemplate(any()) } returns 1L
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `createTemplate computes endTime based on duration`() = runTest {
        viewModel = TemplateViewModel(classTemplateRepository, studioRepository, yogaClassRepository, autoScheduleManager)

        viewModel.createTemplate(
            name = "Dienstag Abend",
            studioId = 2L,
            className = "Hatha",
            dayOfWeek = DayOfWeek.TUESDAY,
            startTime = LocalTime(18, 0),
            duration = 1.5
        )

        advanceUntilIdle()

        coVerify {
            classTemplateRepository.createTemplate(match<ClassTemplate> {
                it.name == "Dienstag Abend" &&
                it.studioId == 2L &&
                it.className == "Hatha" &&
                it.dayOfWeek == DayOfWeek.TUESDAY &&
                it.startTime.hour == 18 && it.startTime.minute == 0 &&
                it.endTime.hour == 19 && it.endTime.minute == 30 &&
                it.duration == 1.5
            })
        }
    }
}

