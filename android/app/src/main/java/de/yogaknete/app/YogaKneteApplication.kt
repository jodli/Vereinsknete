package de.yogaknete.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import de.yogaknete.app.core.notification.NotificationChannelManager
import de.yogaknete.app.domain.worker.AutoScheduleWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class YogaKneteApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        NotificationChannelManager.createChannels(this)
        scheduleAutoScheduleWork()
    }

    private fun scheduleAutoScheduleWork() {
        val workRequest = PeriodicWorkRequestBuilder<AutoScheduleWorker>(1, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "auto_schedule_classes",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
