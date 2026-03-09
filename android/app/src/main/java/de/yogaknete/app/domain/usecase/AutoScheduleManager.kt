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
    
    /**
     * Auto-schedule when a template is enabled for auto-scheduling
     * Catches up on any missed classes within the advance window
     */
    suspend fun autoScheduleOnTemplateEnabled(templateId: Long) {
        val template = classTemplateDao.getTemplateById(templateId) ?: return
        if (!template.autoSchedule || !template.isActive) return

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val advanceDays = getAdvanceDays(template)
        val endDate = today.plus(DatePeriod(days = advanceDays))

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

        templates.forEach { template ->
            val advanceDays = getAdvanceDays(template)
            val endDate = today.plus(DatePeriod(days = advanceDays))
            var date = today
            while (date <= endDate) {
                if (date.dayOfWeek == template.dayOfWeek && shouldSchedule(date, template)) {
                    createClassFromTemplate(template, date)
                    classTemplateDao.updateLastScheduledDate(template.id, date)
                }
                date = date.plus(DatePeriod(days = 1))
            }
        }
    }

    private fun getAdvanceDays(template: ClassTemplate): Int {
        return maxOf(7, template.recurrenceIntervalWeeks * 7)
    }

    private fun isOnRecurrenceWeek(date: LocalDate, template: ClassTemplate): Boolean {
        val refDate = template.referenceDate ?: return true
        if (template.recurrenceIntervalWeeks <= 1) return true
        val daysBetween = refDate.daysUntil(date)
        val weeksBetween = daysBetween / 7
        return weeksBetween >= 0 && weeksBetween % template.recurrenceIntervalWeeks == 0
    }

    private suspend fun shouldSchedule(date: LocalDate, template: ClassTemplate): Boolean {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        // Don't schedule in the past
        if (date < today) return false

        // Only schedule within advance window
        val advanceDays = getAdvanceDays(template)
        if (date > today.plus(DatePeriod(days = advanceDays))) return false

        // Check recurrence interval
        if (!isOnRecurrenceWeek(date, template)) return false

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
}
