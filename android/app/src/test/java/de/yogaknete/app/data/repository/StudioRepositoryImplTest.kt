package de.yogaknete.app.data.repository

import de.yogaknete.app.data.local.StudioDao
import de.yogaknete.app.domain.model.Studio
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class StudioRepositoryImplTest {
    
    private lateinit var studioDao: StudioDao
    private lateinit var repository: StudioRepositoryImpl
    
    @Before
    fun setup() {
        studioDao = mockk()
        repository = StudioRepositoryImpl(studioDao)
    }
    
    @Test
    fun `getAllStudios returns flow from dao`() = runTest {
        // Given
        val studios = listOf(
            createTestStudio(id = 1, name = "Studio 1"),
            createTestStudio(id = 2, name = "Studio 2")
        )
        every { studioDao.getAllStudios() } returns flowOf(studios)
        
        // When
        val result = repository.getAllStudios().first()
        
        // Then
        assertEquals(studios, result)
        verify { studioDao.getAllStudios() }
    }
    
    @Test
    fun `getAllActiveStudios returns only active studios`() = runTest {
        // Given
        val activeStudios = listOf(
            createTestStudio(id = 1, isActive = true),
            createTestStudio(id = 2, isActive = true)
        )
        every { studioDao.getAllActiveStudios() } returns flowOf(activeStudios)
        
        // When
        val result = repository.getAllActiveStudios().first()
        
        // Then
        assertEquals(activeStudios, result)
        assertTrue(result.all { it.isActive })
        verify { studioDao.getAllActiveStudios() }
    }
    
    @Test
    fun `getStudioById returns studio when found`() = runTest {
        // Given
        val studioId = 5L
        val expectedStudio = createTestStudio(id = studioId)
        coEvery { studioDao.getStudioById(studioId) } returns expectedStudio
        
        // When
        val result = repository.getStudioById(studioId)
        
        // Then
        assertEquals(expectedStudio, result)
        coVerify { studioDao.getStudioById(studioId) }
    }
    
    @Test
    fun `getStudioById returns null when not found`() = runTest {
        // Given
        val studioId = 999L
        coEvery { studioDao.getStudioById(studioId) } returns null
        
        // When
        val result = repository.getStudioById(studioId)
        
        // Then
        assertNull(result)
        coVerify { studioDao.getStudioById(studioId) }
    }
    
    @Test
    fun `saveStudio inserts and returns id`() = runTest {
        // Given
        val studio = createTestStudio()
        val expectedId = 10L
        coEvery { studioDao.insertStudio(studio) } returns expectedId
        
        // When
        val result = repository.saveStudio(studio)
        
        // Then
        assertEquals(expectedId, result)
        coVerify { studioDao.insertStudio(studio) }
    }
    
    @Test
    fun `updateStudio calls dao update`() = runTest {
        // Given
        val studio = createTestStudio(id = 1)
        coEvery { studioDao.updateStudio(studio) } just Runs
        
        // When
        repository.updateStudio(studio)
        
        // Then
        coVerify { studioDao.updateStudio(studio) }
    }
    
    @Test
    fun `deleteStudio calls dao delete`() = runTest {
        // Given
        val studio = createTestStudio(id = 1)
        coEvery { studioDao.deleteStudio(studio) } just Runs
        
        // When
        repository.deleteStudio(studio)
        
        // Then
        coVerify { studioDao.deleteStudio(studio) }
    }
    
    @Test
    fun `deactivateStudio sets studio to inactive`() = runTest {
        // Given
        val studioId = 5L
        coEvery { studioDao.deactivateStudio(studioId) } just Runs
        
        // When
        repository.deactivateStudio(studioId)
        
        // Then
        coVerify { studioDao.deactivateStudio(studioId) }
    }
    
    @Test
    fun `getActiveStudioCount returns correct count`() = runTest {
        // Given
        val expectedCount = 3
        coEvery { studioDao.getActiveStudioCount() } returns expectedCount
        
        // When
        val result = repository.getActiveStudioCount()
        
        // Then
        assertEquals(expectedCount, result)
        coVerify { studioDao.getActiveStudioCount() }
    }
    
    @Test
    fun `saveStudios inserts multiple studios`() = runTest {
        // Given
        val studios = listOf(
            createTestStudio(name = "Studio 1"),
            createTestStudio(name = "Studio 2"),
            createTestStudio(name = "Studio 3")
        )
        coEvery { studioDao.insertStudios(studios) } just Runs
        
        // When
        repository.saveStudios(studios)
        
        // Then
        coVerify { studioDao.insertStudios(studios) }
    }
    
    @Test
    fun `deleteAllStudios removes all studios`() = runTest {
        // Given
        coEvery { studioDao.deleteAllStudios() } just Runs
        
        // When
        repository.deleteAllStudios()
        
        // Then
        coVerify { studioDao.deleteAllStudios() }
    }
    
    // Helper function to create test studio
    private fun createTestStudio(
        id: Long = 0,
        name: String = "Test Studio",
        isActive: Boolean = true
    ): Studio {
        return Studio(
            id = id,
            name = name,
            contactPerson = "Test Person",
            email = "test@studio.de",
            phone = "+49 123 456789",
            street = "Test Street 1",
            postalCode = "12345",
            city = "Test City",
            hourlyRate = 65.0,
            isActive = isActive
        )
    }
}
