package de.yogaknete.app.domain.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.Data
import de.yogaknete.app.domain.model.ClassStatus
import de.yogaknete.app.domain.model.Studio
import de.yogaknete.app.domain.model.YogaClass
import de.yogaknete.app.domain.repository.StudioRepository
import de.yogaknete.app.domain.repository.YogaClassRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.unmockkConstructor
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ClassNotificationWorkerTest {

    private lateinit var context: Context
    private lateinit var workerParams: WorkerParameters
    private lateinit var yogaClassRepository: YogaClassRepository
    private lateinit var studioRepository: StudioRepository
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var worker: ClassNotificationWorker

    private val testStudio = Studio(
        id = 1L,
        name = "Sonnenstudio",
        hourlyRate = 30.0
    )

    private val testClass = YogaClass(
        id = 42L,
        studioId = 1L,
        title = "Hatha Yoga",
        startTime = LocalDateTime(2026, 3, 9, 18, 0),
        endTime = LocalDateTime(2026, 3, 9, 19, 30),
        durationHours = 1.5,
        status = ClassStatus.SCHEDULED
    )

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        workerParams = mockk(relaxed = true)
        yogaClassRepository = mockk()
        studioRepository = mockk()
        notificationManager = mockk(relaxed = true)

        every { workerParams.inputData } returns Data.Builder()
            .putLong("classId", 42L)
            .build()

        mockkStatic(NotificationManagerCompat::class)
        every { NotificationManagerCompat.from(any()) } returns notificationManager

        // Mock Intent constructor and methods (Android stubs)
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().putExtra(any<String>(), any<Long>()) } returns mockk(relaxed = true)
        every { anyConstructed<Intent>().setFlags(any()) } returns mockk(relaxed = true)

        // Mock PendingIntent.getActivity
        mockkStatic(PendingIntent::class)
        every { PendingIntent.getActivity(any(), any(), any(), any()) } returns mockk()

        // Mock NotificationCompat.Builder
        mockkConstructor(NotificationCompat.Builder::class)
        every { anyConstructed<NotificationCompat.Builder>().setSmallIcon(any<Int>()) } returns mockk<NotificationCompat.Builder>(relaxed = true) {
            every { setContentTitle(any()) } returns this
            every { setContentText(any()) } returns this
            every { setColor(any()) } returns this
            every { setAutoCancel(any()) } returns this
            every { setContentIntent(any()) } returns this
            every { build() } returns mockk(relaxed = true)
        }

        worker = ClassNotificationWorker(
            context,
            workerParams,
            yogaClassRepository,
            studioRepository
        )
    }

    @After
    fun tearDown() {
        unmockkStatic(NotificationManagerCompat::class)
        unmockkStatic(PendingIntent::class)
        unmockkConstructor(Intent::class)
        unmockkConstructor(NotificationCompat.Builder::class)
    }

    @Test
    fun `doWork shows notification for SCHEDULED class`() = runTest {
        coEvery { yogaClassRepository.getClassById(42L) } returns testClass
        coEvery { studioRepository.getStudioById(1L) } returns testStudio

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        verify { notificationManager.notify(42, any()) }
    }

    @Test
    fun `doWork does not notify for COMPLETED class`() = runTest {
        coEvery { yogaClassRepository.getClassById(42L) } returns testClass.copy(status = ClassStatus.COMPLETED)

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        verify(exactly = 0) { notificationManager.notify(any(), any()) }
    }

    @Test
    fun `doWork does not notify for CANCELLED class`() = runTest {
        coEvery { yogaClassRepository.getClassById(42L) } returns testClass.copy(status = ClassStatus.CANCELLED)

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        verify(exactly = 0) { notificationManager.notify(any(), any()) }
    }

    @Test
    fun `doWork does not notify for deleted class`() = runTest {
        coEvery { yogaClassRepository.getClassById(42L) } returns null

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        verify(exactly = 0) { notificationManager.notify(any(), any()) }
    }

    @Test
    fun `doWork does not notify when studio not found`() = runTest {
        coEvery { yogaClassRepository.getClassById(42L) } returns testClass
        coEvery { studioRepository.getStudioById(1L) } returns null

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        verify(exactly = 0) { notificationManager.notify(any(), any()) }
    }

    @Test
    fun `doWork returns success when no classId in input data`() = runTest {
        every { workerParams.inputData } returns Data.Builder().build()

        worker = ClassNotificationWorker(
            context,
            workerParams,
            yogaClassRepository,
            studioRepository
        )

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        verify(exactly = 0) { notificationManager.notify(any(), any()) }
    }
}
