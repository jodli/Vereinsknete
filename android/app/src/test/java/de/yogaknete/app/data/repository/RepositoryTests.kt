package de.yogaknete.app.data.repository

import de.yogaknete.app.data.local.UserProfileDao
import de.yogaknete.app.data.local.StudioDao
import de.yogaknete.app.domain.model.UserProfile
import de.yogaknete.app.domain.model.Studio
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before

/**
 * Tests for repository implementations
 */
class RepositoryTests {
    
    private lateinit var userProfileDao: UserProfileDao
    private lateinit var studioDao: StudioDao
    private lateinit var userProfileRepository: UserProfileRepositoryImpl
    private lateinit var studioRepository: StudioRepositoryImpl
    
    @Before
    fun setup() {
        userProfileDao = mockk()
        studioDao = mockk()
        userProfileRepository = UserProfileRepositoryImpl(userProfileDao)
        studioRepository = StudioRepositoryImpl(studioDao)
    }
    
    @Test
    fun `UserProfileRepository saves profile correctly`() = runBlocking {
        val profile = UserProfile(
            name = "Maria Schmidt",
            defaultHourlyRate = 31.50,
            isOnboardingComplete = false
        )
        
        coEvery { userProfileDao.insertUserProfile(profile) } just Runs
        
        userProfileRepository.saveUserProfile(profile)
        
        coVerify { userProfileDao.insertUserProfile(profile) }
    }
    
    @Test
    fun `UserProfileRepository checks onboarding status`() = runBlocking {
        val profile = UserProfile(
            name = "Maria Schmidt",
            defaultHourlyRate = 31.50,
            isOnboardingComplete = true
        )
        
        coEvery { userProfileDao.getUserProfileOnce() } returns profile
        
        val isComplete = userProfileRepository.isOnboardingComplete()
        
        assertTrue(isComplete)
        coVerify { userProfileDao.getUserProfileOnce() }
    }
    
    @Test
    fun `StudioRepository saves studio and returns ID`() = runBlocking {
        val studio = Studio(
            name = "TSV München",
            hourlyRate = 35.00
        )
        
        coEvery { studioDao.insertStudio(studio) } returns 42L
        
        val id = studioRepository.saveStudio(studio)
        
        assertEquals(42L, id)
        coVerify { studioDao.insertStudio(studio) }
    }
    
    @Test
    fun `StudioRepository returns active studios flow`() = runBlocking {
        val studios = listOf(
            Studio(id = 1, name = "TSV München", hourlyRate = 35.00),
            Studio(id = 2, name = "SV Neuhausen", hourlyRate = 31.50)
        )
        
        every { studioDao.getAllActiveStudios() } returns flowOf(studios)
        
        val flow = studioRepository.getAllActiveStudios()
        
        verify { studioDao.getAllActiveStudios() }
    }
}
