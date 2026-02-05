package de.yogaknete.app.domain.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.yogaknete.app.domain.usecase.AutoScheduleManager

@HiltWorker
class AutoScheduleWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val autoScheduleManager: AutoScheduleManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            autoScheduleManager.catchUpAutoSchedule()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
