package de.yogaknete.app.domain.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import de.yogaknete.app.domain.usecase.AutoScheduleManager
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AutoScheduleWorkerTest {

    private lateinit var autoScheduleManager: AutoScheduleManager

    @Before
    fun setup() {
        autoScheduleManager = mockk()
    }

    private fun buildWorker(): AutoScheduleWorker {
        return TestListenableWorkerBuilder<AutoScheduleWorker>(
            context = ApplicationProvider.getApplicationContext()
        ).setWorkerFactory(object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker {
                return AutoScheduleWorker(appContext, workerParameters, autoScheduleManager)
            }
        }).build() as AutoScheduleWorker
    }

    @Test
    fun `doWork calls catchUpAutoSchedule and returns success`() = runTest {
        coEvery { autoScheduleManager.catchUpAutoSchedule() } just Runs

        val worker = buildWorker()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        coVerify { autoScheduleManager.catchUpAutoSchedule() }
    }

    @Test
    fun `doWork returns retry on failure`() = runTest {
        coEvery { autoScheduleManager.catchUpAutoSchedule() } throws RuntimeException("DB error")

        val worker = buildWorker()
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
    }
}
