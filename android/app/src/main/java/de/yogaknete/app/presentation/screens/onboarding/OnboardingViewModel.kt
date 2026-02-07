package de.yogaknete.app.presentation.screens.onboarding

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.yogaknete.app.domain.model.BackupMetadata
import de.yogaknete.app.domain.model.ImportResult
import de.yogaknete.app.domain.model.ImportStrategy
import de.yogaknete.app.domain.model.UserProfile
import de.yogaknete.app.domain.model.Studio
import de.yogaknete.app.domain.model.ValidationResult
import de.yogaknete.app.domain.repository.UserProfileRepository
import de.yogaknete.app.domain.repository.StudioRepository
import de.yogaknete.app.domain.service.BackupImportService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RestoreState {
    IDLE,
    PROCESSING,
    PREVIEW,
    SUCCESS,
    ERROR
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val studioRepository: StudioRepository,
    private val backupImportService: BackupImportService
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _onboardingComplete = MutableStateFlow(false)
    val onboardingComplete: StateFlow<Boolean> = _onboardingComplete.asStateFlow()

    private val _restoreState = MutableStateFlow(RestoreState.IDLE)
    val restoreState: StateFlow<RestoreState> = _restoreState.asStateFlow()

    private val _previewMetadata = MutableStateFlow<BackupMetadata?>(null)
    val previewMetadata: StateFlow<BackupMetadata?> = _previewMetadata.asStateFlow()

    private val _restoreErrorMessage = MutableStateFlow<String?>(null)
    val restoreErrorMessage: StateFlow<String?> = _restoreErrorMessage.asStateFlow()

    private var selectedUri: Uri? = null

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

    fun onBackupFileSelected(uri: Uri) {
        selectedUri = uri
        viewModelScope.launch {
            _restoreState.value = RestoreState.PROCESSING
            when (val result = backupImportService.validateBackup(uri)) {
                is ValidationResult.Valid -> {
                    _previewMetadata.value = result.metadata
                    _restoreState.value = RestoreState.PREVIEW
                }
                is ValidationResult.Invalid -> {
                    _restoreErrorMessage.value = result.reason
                    _restoreState.value = RestoreState.ERROR
                }
            }
        }
    }

    fun confirmRestore(strategy: ImportStrategy) {
        val uri = selectedUri ?: return
        viewModelScope.launch {
            _restoreState.value = RestoreState.PROCESSING
            when (val result = backupImportService.importBackup(uri, strategy)) {
                is ImportResult.Success -> {
                    _restoreState.value = RestoreState.SUCCESS
                    // Re-check onboarding status — the imported backup likely
                    // contains a UserProfile with isOnboardingComplete = true
                    checkOnboardingStatus()
                }
                is ImportResult.Error -> {
                    _restoreErrorMessage.value = result.message
                    _restoreState.value = RestoreState.ERROR
                }
            }
        }
    }

    fun cancelRestore() {
        selectedUri = null
        _previewMetadata.value = null
        _restoreState.value = RestoreState.IDLE
    }

    fun dismissImportError() {
        _restoreErrorMessage.value = null
        _restoreState.value = RestoreState.IDLE
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
