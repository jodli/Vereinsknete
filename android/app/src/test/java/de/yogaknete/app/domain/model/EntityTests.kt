package de.yogaknete.app.domain.model

import kotlinx.datetime.LocalDateTime
import org.junit.Test
import org.junit.Assert.*

/**
 * Basic tests to verify our data models work correctly
 */
class EntityTests {
    
    @Test
    fun `UserProfile creation with correct defaults`() {
        val profile = UserProfile(
            name = "Maria Schmidt",
            defaultHourlyRate = 31.50
        )
        
        assertEquals("Maria Schmidt", profile.name)
        assertEquals(31.50, profile.defaultHourlyRate, 0.01)
        assertFalse(profile.isOnboardingComplete)
        assertEquals(1L, profile.id)
    }
    
    @Test
    fun `Studio creation with correct defaults`() {
        val studio = Studio(
            name = "TSV München",
            hourlyRate = 35.00
        )
        
        assertEquals("TSV München", studio.name)
        assertEquals(35.00, studio.hourlyRate, 0.01)
        assertTrue(studio.isActive)
        assertEquals(0L, studio.id) // Auto-generate ID starts at 0
    }
    
    @Test
    fun `YogaClass creation with all properties`() {
        val startTime = LocalDateTime(2024, 11, 4, 17, 30)
        val endTime = LocalDateTime(2024, 11, 4, 18, 45)
        
        val yogaClass = YogaClass(
            studioId = 1L,
            title = "Hatha Yoga",
            startTime = startTime,
            endTime = endTime,
            durationHours = 1.25
        )
        
        assertEquals(1L, yogaClass.studioId)
        assertEquals("Hatha Yoga", yogaClass.title)
        assertEquals(startTime, yogaClass.startTime)
        assertEquals(endTime, yogaClass.endTime)
        assertEquals(1.25, yogaClass.durationHours, 0.01)
        assertEquals(ClassStatus.SCHEDULED, yogaClass.status)
        assertEquals("", yogaClass.notes)
    }
    
    @Test
    fun `ClassStatus enum has correct values`() {
        val statuses = ClassStatus.values()
        assertEquals(3, statuses.size)
        assertTrue(statuses.contains(ClassStatus.SCHEDULED))
        assertTrue(statuses.contains(ClassStatus.COMPLETED))
        assertTrue(statuses.contains(ClassStatus.CANCELLED))
    }
}
