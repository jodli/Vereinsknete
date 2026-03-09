package de.yogaknete.app.domain.service

import de.yogaknete.app.domain.model.YogaClass

interface ClassNotificationScheduler {
    fun schedule(yogaClass: YogaClass)
    fun cancel(classId: Long)
}
