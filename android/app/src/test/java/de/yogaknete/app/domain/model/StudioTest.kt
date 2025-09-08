package de.yogaknete.app.domain.model

import org.junit.Assert.*
import org.junit.Test

class StudioTest {
    
    @Test
    fun `create Studio with required fields only`() {
        // Given
        val name = "Yoga Studio München"
        val hourlyRate = 65.0
        
        // When
        val studio = Studio(
            name = name,
            hourlyRate = hourlyRate
        )
        
        // Then
        assertEquals(0L, studio.id)
        assertEquals(name, studio.name)
        assertEquals(hourlyRate, studio.hourlyRate, 0.001)
        assertTrue(studio.isActive)
        // Check default values
        assertEquals("", studio.contactPerson)
        assertEquals("", studio.email)
        assertEquals("", studio.phone)
        assertEquals("", studio.street)
        assertEquals("", studio.postalCode)
        assertEquals("", studio.city)
    }
    
    @Test
    fun `create Studio with all fields`() {
        // Given
        val studio = Studio(
            id = 5L,
            name = "YogaWorks Berlin",
            contactPerson = "Anna Schmidt",
            email = "info@yogaworks.de",
            phone = "+49 30 123456",
            street = "Hauptstraße 123",
            postalCode = "10115",
            city = "Berlin",
            hourlyRate = 75.0,
            isActive = false
        )
        
        // Then
        assertEquals(5L, studio.id)
        assertEquals("YogaWorks Berlin", studio.name)
        assertEquals("Anna Schmidt", studio.contactPerson)
        assertEquals("info@yogaworks.de", studio.email)
        assertEquals("+49 30 123456", studio.phone)
        assertEquals("Hauptstraße 123", studio.street)
        assertEquals("10115", studio.postalCode)
        assertEquals("Berlin", studio.city)
        assertEquals(75.0, studio.hourlyRate, 0.001)
        assertFalse(studio.isActive)
    }
    
    @Test
    fun `verify Studio hourly rate precision`() {
        // Given
        val studio = Studio(
            name = "Test Studio",
            hourlyRate = 67.50
        )
        
        // Then
        assertEquals(67.50, studio.hourlyRate, 0.001)
    }
}
