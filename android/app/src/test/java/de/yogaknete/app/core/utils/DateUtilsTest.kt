package de.yogaknete.app.core.utils

import kotlinx.datetime.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests for DateUtils - the date/time helper functions
 */
class DateUtilsTest {
    
    @Test
    fun `getWeekStart returns Monday of the week`() {
        // Given a Wednesday
        val wednesday = LocalDate(2024, 11, 6) // Nov 6, 2024 is a Wednesday
        
        // When getting week start
        val weekStart = DateUtils.getWeekStart(wednesday)
        
        // Then it should be Monday
        assertEquals(LocalDate(2024, 11, 4), weekStart) // Nov 4, 2024 is Monday
        assertEquals(DayOfWeek.MONDAY, weekStart.dayOfWeek)
    }
    
    @Test
    fun `getWeekEnd returns Sunday of the week`() {
        // Given a Wednesday
        val wednesday = LocalDate(2024, 11, 6)
        
        // When getting week end
        val weekEnd = DateUtils.getWeekEnd(wednesday)
        
        // Then it should be Sunday
        assertEquals(LocalDate(2024, 11, 10), weekEnd) // Nov 10, 2024 is Sunday
        assertEquals(DayOfWeek.SUNDAY, weekEnd.dayOfWeek)
    }
    
    @Test
    fun `getWeekDays returns all 7 days of the week`() {
        // Given a date
        val date = LocalDate(2024, 11, 6)
        
        // When getting week days
        val weekDays = DateUtils.getWeekDays(date)
        
        // Then
        assertEquals(7, weekDays.size)
        assertEquals(DayOfWeek.MONDAY, weekDays.first().dayOfWeek)
        assertEquals(DayOfWeek.SUNDAY, weekDays.last().dayOfWeek)
    }
    
    @Test
    fun `formatWeekRange formats in German`() {
        val start = LocalDate(2024, 11, 4)
        val end = LocalDate(2024, 11, 10)
        
        val formatted = DateUtils.formatWeekRange(start, end)
        
        // Should be in German format
        assertTrue(formatted.contains("4. - 10."))
        assertTrue(formatted.contains("November"))
    }
    
    @Test
    fun `formatDayDate formats in German uppercase`() {
        val monday = LocalDate(2024, 11, 4)
        
        val formatted = DateUtils.formatDayDate(monday)
        
        // Should contain German day name in uppercase
        assertTrue(formatted.contains("MONTAG"))
        assertTrue(formatted.contains("4. Nov"))
    }
    
    @Test
    fun `formatTimeRange creates proper time range string`() {
        val start = LocalDateTime(2024, 11, 4, 17, 30)
        val end = LocalDateTime(2024, 11, 4, 18, 45)
        
        val formatted = DateUtils.formatTimeRange(start, end)
        
        assertEquals("17:30 - 18:45", formatted)
    }
    
    @Test
    fun `calculateDurationHours calculates correctly`() {
        val start = LocalDateTime(2024, 11, 4, 17, 30)
        val end = LocalDateTime(2024, 11, 4, 18, 45)
        
        val duration = DateUtils.calculateDurationHours(start, end)
        
        assertEquals(1.25, duration, 0.01)
    }
}
