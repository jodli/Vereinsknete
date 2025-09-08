package de.yogaknete.app.core.utils

import kotlinx.datetime.*
import java.time.format.TextStyle
import java.util.Locale

object DateUtils {
    
    /**
     * Get the start of the week (Monday) for a given date
     */
    fun getWeekStart(date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): LocalDate {
        val dayOfWeek = date.dayOfWeek
        val daysToSubtract = when (dayOfWeek) {
            DayOfWeek.MONDAY -> 0
            DayOfWeek.TUESDAY -> 1
            DayOfWeek.WEDNESDAY -> 2
            DayOfWeek.THURSDAY -> 3
            DayOfWeek.FRIDAY -> 4
            DayOfWeek.SATURDAY -> 5
            DayOfWeek.SUNDAY -> 6
        }
        return date.minus(DatePeriod(days = daysToSubtract))
    }
    
    /**
     * Get the end of the week (Sunday) for a given date
     */
    fun getWeekEnd(date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): LocalDate {
        val weekStart = getWeekStart(date)
        return weekStart.plus(DatePeriod(days = 6))
    }
    
    /**
     * Get all days of the week for a given date
     */
    fun getWeekDays(date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): List<LocalDate> {
        val weekStart = getWeekStart(date)
        return (0..6).map { weekStart.plus(DatePeriod(days = it)) }
    }
    
    /**
     * Format a date range for display (e.g., "4. - 10. November")
     */
    fun formatWeekRange(startDate: LocalDate, endDate: LocalDate): String {
        val locale = Locale.GERMAN
        val month = endDate.month.getDisplayName(TextStyle.FULL, locale)
        return "${startDate.dayOfMonth}. - ${endDate.dayOfMonth}. $month"
    }
    
    /**
     * Format day and date (e.g., "MONTAG, 4. Nov")
     */
    fun formatDayDate(date: LocalDate): String {
        val locale = Locale.GERMAN
        val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale).uppercase()
        val monthShort = date.month.getDisplayName(TextStyle.SHORT, locale)
        return "$dayName, ${date.dayOfMonth}. $monthShort"
    }
    
    /**
     * Format time range (e.g., "17:30 - 18:45")
     */
    fun formatTimeRange(startTime: LocalDateTime, endTime: LocalDateTime): String {
        return "${formatTime(startTime)} - ${formatTime(endTime)}"
    }
    
    /**
     * Format time (e.g., "17:30")
     */
    fun formatTime(dateTime: LocalDateTime): String {
        return String.format(Locale.GERMAN, "%02d:%02d", dateTime.hour, dateTime.minute)
    }
    
    /**
     * Calculate duration in hours between two times
     */
    fun calculateDurationHours(startTime: LocalDateTime, endTime: LocalDateTime): Double {
        val duration = endTime.toInstant(TimeZone.currentSystemDefault()) - 
                      startTime.toInstant(TimeZone.currentSystemDefault())
        return duration.inWholeMinutes / 60.0
    }
    
    /**
     * Check if a date is today
     */
    fun isToday(date: LocalDate): Boolean {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return date == today
    }
    
    /**
     * Check if a date is in the past
     */
    fun isPast(date: LocalDate): Boolean {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        return date < today
    }
}
