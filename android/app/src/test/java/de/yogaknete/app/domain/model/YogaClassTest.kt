package de.yogaknete.app.domain.model

import kotlinx.datetime.LocalDateTime
import org.junit.Assert.*
import org.junit.Test

class YogaClassTest {
    
    @Test
    fun `create YogaClass with default values`() {
        // Given
        val studioId = 1L
        val title = "Hatha Yoga"
        val startTime = LocalDateTime(2024, 1, 15, 10, 0)
        val endTime = LocalDateTime(2024, 1, 15, 11, 30)
        val duration = 1.5
        
        // When
        val yogaClass = YogaClass(
            studioId = studioId,
            title = title,
            startTime = startTime,
            endTime = endTime,
            durationHours = duration
        )
        
        // Then
        assertEquals(0L, yogaClass.id)
        assertEquals(studioId, yogaClass.studioId)
        assertEquals(title, yogaClass.title)
        assertEquals(startTime, yogaClass.startTime)
        assertEquals(endTime, yogaClass.endTime)
        assertEquals(duration, yogaClass.durationHours, 0.001)
        assertEquals(ClassStatus.SCHEDULED, yogaClass.status)
        assertEquals("", yogaClass.notes)
    }
    
    @Test
    fun `create YogaClass with custom status and notes`() {
        // Given
        val status = ClassStatus.COMPLETED
        val notes = "Great session with beginners"
        
        // When
        val yogaClass = YogaClass(
            studioId = 1L,
            title = "Yoga für Anfänger",
            startTime = LocalDateTime(2024, 1, 15, 10, 0),
            endTime = LocalDateTime(2024, 1, 15, 11, 0),
            durationHours = 1.0,
            status = status,
            notes = notes
        )
        
        // Then
        assertEquals(status, yogaClass.status)
        assertEquals(notes, yogaClass.notes)
    }
    
    @Test
    fun `verify all ClassStatus values exist`() {
        // Verify enum values are available
        assertNotNull(ClassStatus.SCHEDULED)
        assertNotNull(ClassStatus.COMPLETED)
        assertNotNull(ClassStatus.CANCELLED)
        
        // Verify we have exactly 3 status values
        assertEquals(3, ClassStatus.values().size)
    }
}
