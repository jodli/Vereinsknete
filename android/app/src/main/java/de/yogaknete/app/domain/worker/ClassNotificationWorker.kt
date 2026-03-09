package de.yogaknete.app.domain.worker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.yogaknete.app.MainActivity
import de.yogaknete.app.R
import de.yogaknete.app.core.notification.NotificationChannelManager
import de.yogaknete.app.domain.model.ClassStatus
import de.yogaknete.app.domain.repository.StudioRepository
import de.yogaknete.app.domain.repository.YogaClassRepository

@HiltWorker
class ClassNotificationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val yogaClassRepository: YogaClassRepository,
    private val studioRepository: StudioRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val INPUT_CLASS_ID = "classId"
        private const val YOGA_PRIMARY_COLOR = 0xFF6B4EE6.toInt()
    }

    override suspend fun doWork(): Result {
        val classId = inputData.getLong(INPUT_CLASS_ID, -1L)
        if (classId == -1L) return Result.success()

        val yogaClass = yogaClassRepository.getClassById(classId)
            ?: return Result.success()

        if (yogaClass.status != ClassStatus.SCHEDULED) return Result.success()

        val studio = studioRepository.getStudioById(yogaClass.studioId)
            ?: return Result.success()

        val title = "${yogaClass.title} bei ${studio.name}"
        val startTimeFormatted = "%02d:%02d".format(
            yogaClass.startTime.hour,
            yogaClass.startTime.minute
        )
        val body = "Hat dein Kurs um $startTimeFormatted stattgefunden?"

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra("classId", classId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            classId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationChannelManager.CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_yoga_pose)
            .setContentTitle(title)
            .setContentText(body)
            .setColor(YOGA_PRIMARY_COLOR)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        NotificationManagerCompat.from(applicationContext).notify(classId.toInt(), notification)

        return Result.success()
    }
}
