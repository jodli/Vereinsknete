package de.yogaknete.app.data.notification

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import de.yogaknete.app.domain.model.YogaClass
import de.yogaknete.app.domain.service.ClassNotificationScheduler
import de.yogaknete.app.domain.worker.ClassNotificationWorker
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes

class ClassNotificationSchedulerImpl @Inject constructor(
    private val workManager: WorkManager,
    private val clock: Clock
) : ClassNotificationScheduler {

    override fun schedule(yogaClass: YogaClass) {
        val notificationTime = yogaClass.endTime
            .toInstant(TimeZone.currentSystemDefault()) + 45.minutes
        val now = clock.now()
        val delayMs = (notificationTime - now).inWholeMilliseconds.coerceAtLeast(0)

        val workRequest = OneTimeWorkRequestBuilder<ClassNotificationWorker>()
            .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(ClassNotificationWorker.INPUT_CLASS_ID to yogaClass.id)
            )
            .build()

        workManager.enqueueUniqueWork(
            uniqueWorkName(yogaClass.id),
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    override fun cancel(classId: Long) {
        workManager.cancelUniqueWork(uniqueWorkName(classId))
    }

    private fun uniqueWorkName(classId: Long) = "class_notification_$classId"
}
