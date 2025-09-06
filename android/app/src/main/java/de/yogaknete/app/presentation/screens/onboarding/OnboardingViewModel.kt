package de.yogaknete.app.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.yogaknete.app.domain.model.UserProfile
import de.yogaknete.app.domain.model.Studio
import de.yogaknete.app.domain.repository.UserProfileRepository
import de.yogaknete.app.domain.repository.StudioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val studioRepository: StudioRepository
) : ViewModel() {
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _onboardingComplete = MutableStateFlow(false)
    val onboardingComplete: StateFlow<Boolean> = _onboardingComplete.asStateFlow()
    
    init {
        checkOnboardingStatus()
    }
    
    private fun checkOnboardingStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val isComplete = userProfileRepository.isOnboardingComplete()
                _onboardingComplete.value = isComplete
            } catch (e: Exception) {
                // Handle error - for now just assume onboarding is not complete
                _onboardingComplete.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun completeOnboarding(
        userProfile: UserProfile,
        studios: List<StudioInput>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Save user profile with onboarding complete flag
                val completeProfile = userProfile.copy(isOnboardingComplete = true)
                userProfileRepository.saveUserProfile(completeProfile)
                
                // Save studios
                if (studios.isNotEmpty()) {
                    val studioEntities = studios.map { studioInput ->
                        Studio(
                            name = studioInput.name,
                            contactPerson = studioInput.contactPerson,
                            email = studioInput.email,
                            phone = studioInput.phone,
                            street = studioInput.street,
                            postalCode = studioInput.postalCode,
                            city = studioInput.city,
                            hourlyRate = studioInput.hourlyRate
                        )
                    }
                    studioRepository.saveStudios(studioEntities)
                }
                
                _onboardingComplete.value = true
            } catch (e: Exception) {
                // Handle error - in a real app you'd want proper error handling
                // For now, we'll just log it (in a real app you'd use proper logging)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
