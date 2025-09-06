package de.yogaknete.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.yogaknete.app.domain.model.UserProfile
import de.yogaknete.app.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserProfileEditUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class UserProfileEditViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UserProfileEditUiState())
    val uiState: StateFlow<UserProfileEditUiState> = _uiState.asStateFlow()
    
    init {
        loadUserProfile()
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val profile = userProfileRepository.getUserProfileOnce()
                _uiState.value = _uiState.value.copy(
                    profile = profile,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Fehler beim Laden des Profils: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                userProfileRepository.saveUserProfile(profile)
                _uiState.value = _uiState.value.copy(
                    profile = profile,
                    isLoading = false,
                    isSaved = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Fehler beim Speichern des Profils: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun resetSaveState() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }
}
