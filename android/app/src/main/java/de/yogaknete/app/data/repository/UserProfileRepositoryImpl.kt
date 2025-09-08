package de.yogaknete.app.data.repository

import de.yogaknete.app.data.local.UserProfileDao
import de.yogaknete.app.domain.model.UserProfile
import de.yogaknete.app.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val userProfileDao: UserProfileDao
) : UserProfileRepository {
    
    override fun getUserProfile(): Flow<UserProfile?> {
        return userProfileDao.getUserProfile()
    }
    
    override suspend fun getUserProfileOnce(): UserProfile? {
        return userProfileDao.getUserProfileOnce()
    }
    
    override suspend fun saveUserProfile(userProfile: UserProfile) {
        userProfileDao.insertUserProfile(userProfile)
    }
    
    override suspend fun updateUserProfile(userProfile: UserProfile) {
        userProfileDao.updateUserProfile(userProfile)
    }
    
    override suspend fun completeOnboarding() {
        userProfileDao.updateOnboardingStatus(true)
    }
    
    override suspend fun isOnboardingComplete(): Boolean {
        val profile = userProfileDao.getUserProfileOnce()
        return profile?.isOnboardingComplete == true
    }
    
    override suspend fun insertOrUpdate(userProfile: UserProfile) {
        val existing = userProfileDao.getUserProfileOnce()
        if (existing != null) {
            userProfileDao.updateUserProfile(userProfile)
        } else {
            userProfileDao.insertUserProfile(userProfile)
        }
    }
}
