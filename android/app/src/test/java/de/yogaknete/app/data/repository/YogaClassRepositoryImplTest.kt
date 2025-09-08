package de.yogaknete.app.data.repository

import de.yogaknete.app.data.local.YogaClassDao
import de.yogaknete.app.domain.model.ClassStatus
import de.yogaknete.app.domain.model.YogaClass
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class YogaClassRepositoryImplTest {
    
    private lateinit var yogaClassDao: YogaClassDao
    private lateinit var repository: YogaClassRepositoryImpl
    
    @Before
    fun setup() {
        yogaClassDao = mockk()
        repository = YogaClassRepositoryImpl(yogaClassDao)
    }
    
    @Test
    fun `getClassesForWeek converts dates to DateTime correctly`() = runTest {
        // Given
        val startDate = LocalDate(2024, 1, 15)
        val endDate = LocalDate(2024, 1, 21)
        val expectedStartDateTime = LocalDateTime(2024, 1, 15, 0, 0)
        val expectedEndDateTime = LocalDateTime(2024, 1, 21, 23, 59, 59)
        val mockClasses = listOf(createTestYogaClass())
        
        every { 
            yogaClassDao.getClassesInRange(expectedStartDateTime, expectedEndDateTime) 
        } returns flowOf(mockClasses)
        
        // When
        val result = repository.getClassesForWeek(startDate, endDate).first()
        
        // Then
        assertEquals(mockClasses, result)
        verify { yogaClassDao.getClassesInRange(expectedStartDateTime, expectedEndDateTime) }
    }
    
    @Test
    fun `addClass inserts class and returns id`() = runTest {
        // Given
        val yogaClass = createTestYogaClass()
        val expectedId = 42L
        
        coEvery { yogaClassDao.insertClass(yogaClass) } returns expectedId
        
        // When
        val result = repository.addClass(yogaClass)
        
        // Then
        assertEquals(expectedId, result)
        coVerify { yogaClassDao.insertClass(yogaClass) }
    }
    
    @Test
    fun `updateClass calls dao update`() = runTest {
        // Given
        val yogaClass = createTestYogaClass()
        
        coEvery { yogaClassDao.updateClass(yogaClass) } just Runs
        
        // When
        repository.updateClass(yogaClass)
        
        // Then
        coVerify { yogaClassDao.updateClass(yogaClass) }
    }
    
    @Test
    fun `deleteClass calls dao delete`() = runTest {
        // Given
        val yogaClass = createTestYogaClass()
        
        coEvery { yogaClassDao.deleteClass(yogaClass) } just Runs
        
        // When
        repository.deleteClass(yogaClass)
        
        // Then
        coVerify { yogaClassDao.deleteClass(yogaClass) }
    }
    
    @Test
    fun `getClassById returns class from dao`() = runTest {
        // Given
        val classId = 10L
        val expectedClass = createTestYogaClass(id = classId)
        
        coEvery { yogaClassDao.getClassById(classId) } returns expectedClass
        
        // When
        val result = repository.getClassById(classId)
        
        // Then
        assertEquals(expectedClass, result)
        coVerify { yogaClassDao.getClassById(classId) }
    }
    
    @Test
    fun `getClassById returns null when class not found`() = runTest {
        // Given
        val classId = 999L
        
        coEvery { yogaClassDao.getClassById(classId) } returns null
        
        // When
        val result = repository.getClassById(classId)
        
        // Then
        assertNull(result)
        coVerify { yogaClassDao.getClassById(classId) }
    }
    
    @Test
    fun `getClassesForInvoice retrieves classes for studio and month`() = runTest {
        // Given
        val studioId = 1L
        val month = 11
        val year = 2024
        val expectedClasses = listOf(
            createTestYogaClass(studioId = studioId),
            createTestYogaClass(studioId = studioId)
        )
        
        coEvery { 
            yogaClassDao.getClassesForStudioInMonth(studioId, month, year) 
        } returns expectedClasses
        
        // When
        val result = repository.getClassesForInvoice(studioId, month, year)
        
        // Then
        assertEquals(expectedClasses, result)
        coVerify { yogaClassDao.getClassesForStudioInMonth(studioId, month, year) }
    }
    
    @Test
    fun `getAllClasses returns flow from dao`() = runTest {
        // Given
        val expectedClasses = listOf(
            createTestYogaClass(id = 1),
            createTestYogaClass(id = 2)
        )
        
        every { yogaClassDao.getAllClasses() } returns flowOf(expectedClasses)
        
        // When
        val result = repository.getAllClasses().first()
        
        // Then
        assertEquals(expectedClasses, result)
        verify { yogaClassDao.getAllClasses() }
    }
    
    @Test
    fun `deleteAllClasses calls dao deleteAll`() = runTest {
        // Given
        coEvery { yogaClassDao.deleteAllClasses() } just Runs
        
        // When
        repository.deleteAllClasses()
        
        // Then
        coVerify { yogaClassDao.deleteAllClasses() }
    }
    
    // Helper function to create test data
    private fun createTestYogaClass(
        id: Long = 0,
        studioId: Long = 1
    ): YogaClass {
        return YogaClass(
            id = id,
            studioId = studioId,
            title = "Test Yoga Class",
            startTime = LocalDateTime(2024, 1, 15, 10, 0),
            endTime = LocalDateTime(2024, 1, 15, 11, 30),
            durationHours = 1.5,
            status = ClassStatus.SCHEDULED,
            notes = "Test notes"
        )
    }
}
