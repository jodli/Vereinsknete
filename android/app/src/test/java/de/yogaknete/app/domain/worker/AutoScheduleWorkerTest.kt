package de.yogaknete.app.domain.worker

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import de.yogaknete.app.domain.usecase.AutoScheduleManager
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AutoScheduleWorkerTest {

    private lateinit var context: Context
    private lateinit var workerParams: WorkerParameters
    private lateinit var autoScheduleManager: AutoScheduleManager
    private lateinit var worker: AutoScheduleWorker

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        workerParams = mockk(relaxed = true)
        autoScheduleManager = mockk()
        worker = AutoScheduleWorker(context, workerParams, autoScheduleManager)
    }

    @Test
    fun `doWork calls catchUpAutoSchedule and returns success`() = runTest {
        coEvery { autoScheduleManager.catchUpAutoSchedule() } just Runs

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify { autoScheduleManager.catchUpAutoSchedule() }
    }

    @Test
    fun `doWork returns retry on failure`() = runTest {
        coEvery { autoScheduleManager.catchUpAutoSchedule() } throws RuntimeException("DB error")

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
    }
}
