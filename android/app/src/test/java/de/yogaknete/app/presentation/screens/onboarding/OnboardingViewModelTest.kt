package de.yogaknete.app.presentation.screens.onboarding

import de.yogaknete.app.domain.model.Studio
import de.yogaknete.app.domain.model.UserProfile
import de.yogaknete.app.domain.repository.StudioRepository
import de.yogaknete.app.domain.repository.UserProfileRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Test the OnboardingViewModel business logic
 */
@ExperimentalCoroutinesApi
class OnboardingViewModelTest {
    
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var studioRepository: StudioRepository
    private lateinit var viewModel: OnboardingViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        userProfileRepository = mockk()
        studioRepository = mockk()
        
        // Default mock behavior
        coEvery { userProfileRepository.isOnboardingComplete() } returns false
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `ViewModel checks onboarding status on init`() = runTest {
        coEvery { userProfileRepository.isOnboardingComplete() } returns true
        
        viewModel = OnboardingViewModel(userProfileRepository, studioRepository)
        advanceUntilIdle()
        
        assertTrue(viewModel.onboardingComplete.value)
        coVerify { userProfileRepository.isOnboardingComplete() }
    }
    
    @Test
    fun `completeOnboarding saves user profile and studios`() = runTest {
        coEvery { userProfileRepository.saveUserProfile(any()) } just Runs
        coEvery { studioRepository.saveStudios(any()) } just Runs
        
        viewModel = OnboardingViewModel(userProfileRepository, studioRepository)
        
        val studios = listOf(
            StudioInput(
                name = "TSV München",
                hourlyRate = 35.00
            ),
            StudioInput(
                name = "SV Neuhausen",
                hourlyRate = 31.50
            )
        )
        
        val userProfile = UserProfile(
            name = "Maria Schmidt",
            defaultHourlyRate = 31.50
        )
        
        viewModel.completeOnboarding(
            userProfile = userProfile,
            studios = studios
        )
        
        advanceUntilIdle()
        
        // Verify user profile was saved
        coVerify {
            userProfileRepository.saveUserProfile(
                match { profile ->
                    profile.name == "Maria Schmidt" &&
                    profile.defaultHourlyRate == 31.50 &&
                    profile.isOnboardingComplete
                }
            )
        }
        
        // Verify studios were saved
        coVerify {
            studioRepository.saveStudios(
                match { savedStudios ->
                    savedStudios.size == 2 &&
                    savedStudios[0].name == "TSV München" &&
                    savedStudios[0].hourlyRate == 35.00 &&
                    savedStudios[1].name == "SV Neuhausen" &&
                    savedStudios[1].hourlyRate == 31.50
                }
            )
        }
        
        assertTrue(viewModel.onboardingComplete.value)
    }
    
    @Test
    fun `completeOnboarding works without studios`() = runTest {
        coEvery { userProfileRepository.saveUserProfile(any()) } just Runs
        
        viewModel = OnboardingViewModel(userProfileRepository, studioRepository)
        
        val userProfile = UserProfile(
            name = "Maria Schmidt",
            defaultHourlyRate = 31.50
        )
        
        viewModel.completeOnboarding(
            userProfile = userProfile,
            studios = emptyList()
        )
        
        advanceUntilIdle()
        
        coVerify { userProfileRepository.saveUserProfile(any()) }
        coVerify(exactly = 0) { studioRepository.saveStudios(any()) }
        assertTrue(viewModel.onboardingComplete.value)
    }
}
