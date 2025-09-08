package de.yogaknete.app.domain.usecase

import de.yogaknete.app.data.local.YogaClassDao
import de.yogaknete.app.data.local.dao.ClassTemplateDao
import de.yogaknete.app.data.local.entities.ClassTemplate
import de.yogaknete.app.domain.model.ClassStatus
import de.yogaknete.app.domain.model.CreationSource
import de.yogaknete.app.domain.model.YogaClass
import kotlinx.datetime.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages automatic scheduling of recurring classes from templates
 */
@Singleton
class AutoScheduleManager @Inject constructor(
    private val classTemplateDao: ClassTemplateDao,
    private val yogaClassDao: YogaClassDao
) {
    
    companion object {
        private const val SCHEDULE_ADVANCE_DAYS = 7
    }
    
    /**
     * Auto-schedule classes for the given week
     * @param weekStart First day of the week (Monday)
     */
    suspend fun autoScheduleForWeek(weekStart: LocalDate) {
        // Get all active templates with auto-schedule enabled
        val templates = classTemplateDao.getAutoScheduleTemplates()
        
        templates.forEach { template ->
            // Find the date in this week matching the template's day
            val targetDate = findDateInWeek(weekStart, template.dayOfWeek)
            
            // Check if we should schedule this class
            if (shouldSchedule(targetDate, template)) {
                createClassFromTemplate(template, targetDate)
                classTemplateDao.updateLastScheduledDate(template.id, targetDate)
            }
        }
    }
    
    /**
     * Auto-schedule when a template is enabled for auto-scheduling
     * Catches up on any missed classes within the advance window
     */
    suspend fun autoScheduleOnTemplateEnabled(templateId: Long) {
        val template = classTemplateDao.getTemplateById(templateId) ?: return
        if (!template.autoSchedule || !template.isActive) return
        
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val endDate = today.plus(DatePeriod(days = SCHEDULE_ADVANCE_DAYS))
        
        // Find all occurrences of this template's day within the window
        var currentDate = today
        while (currentDate <= endDate) {
            if (currentDate.dayOfWeek == template.dayOfWeek) {
                if (shouldSchedule(currentDate, template)) {
                    createClassFromTemplate(template, currentDate)
                    classTemplateDao.updateLastScheduledDate(template.id, currentDate)
                }
            }
            currentDate = currentDate.plus(DatePeriod(days = 1))
        }
    }
    
    /**
     * Check all templates and schedule any missing classes
     * Useful for app startup or manual trigger
     */
    suspend fun catchUpAutoSchedule() {
        val templates = classTemplateDao.getAutoScheduleTemplates()
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val weekStart = getWeekStart(today)
        
        // Check current week and next week if within window
        autoScheduleForWeek(weekStart)
        
        val nextWeekStart = weekStart.plus(DatePeriod(days = 7))
        val nextWeekFirstDay = nextWeekStart
        if (nextWeekFirstDay <= today.plus(DatePeriod(days = SCHEDULE_ADVANCE_DAYS))) {
            autoScheduleForWeek(nextWeekStart)
        }
    }
    
    private fun findDateInWeek(weekStart: LocalDate, dayOfWeek: DayOfWeek): LocalDate {
        // Week starts on Monday
        val daysFromMonday = when (dayOfWeek) {
            DayOfWeek.MONDAY -> 0
            DayOfWeek.TUESDAY -> 1
            DayOfWeek.WEDNESDAY -> 2
            DayOfWeek.THURSDAY -> 3
            DayOfWeek.FRIDAY -> 4
            DayOfWeek.SATURDAY -> 5
            DayOfWeek.SUNDAY -> 6
        }
        return weekStart.plus(DatePeriod(days = daysFromMonday))
    }
    
    private suspend fun shouldSchedule(date: LocalDate, template: ClassTemplate): Boolean {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        
        // Don't schedule in the past
        if (date < today) return false
        
        // Only schedule within advance window
        if (date > today.plus(DatePeriod(days = SCHEDULE_ADVANCE_DAYS))) return false
        
        // Check if already scheduled for this date
        if (template.lastScheduledDate == date) return false
        
        // Check if a class already exists at this time
        val startDateTime = LocalDateTime(
            date.year,
            date.monthNumber,
            date.dayOfMonth,
            template.startTime.hour,
            template.startTime.minute
        )
        
        val existingClass = yogaClassDao.getClassAtTime(startDateTime, template.studioId)
        return existingClass == null
    }
    
    private suspend fun createClassFromTemplate(template: ClassTemplate, date: LocalDate) {
        val startDateTime = LocalDateTime(
            date.year,
            date.monthNumber,
            date.dayOfMonth,
            template.startTime.hour,
            template.startTime.minute
        )
        
        val endDateTime = LocalDateTime(
            date.year,
            date.monthNumber,
            date.dayOfMonth,
            template.endTime.hour,
            template.endTime.minute
        )
        
        val newClass = YogaClass(
            studioId = template.studioId,
            title = template.className,
            startTime = startDateTime,
            endTime = endDateTime,
            durationHours = template.duration,
            status = ClassStatus.SCHEDULED,
            creationSource = CreationSource.AUTO,
            sourceTemplateId = template.id
        )
        
        yogaClassDao.insertClass(newClass)
    }
    
    private fun getWeekStart(date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())): LocalDate {
        // Find the most recent Monday
        var weekStart = date
        while (weekStart.dayOfWeek != DayOfWeek.MONDAY) {
            weekStart = weekStart.minus(DatePeriod(days = 1))
        }
        return weekStart
    }
}
