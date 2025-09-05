package de.yogaknete.app.domain.repository

import de.yogaknete.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    
    fun getUserProfile(): Flow<UserProfile?>
    
    suspend fun getUserProfileOnce(): UserProfile?
    
    suspend fun saveUserProfile(userProfile: UserProfile)
    
    suspend fun updateUserProfile(userProfile: UserProfile)
    
    suspend fun completeOnboarding()
    
    suspend fun isOnboardingComplete(): Boolean
}
