package de.yogaknete.app.data.notification

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import de.yogaknete.app.domain.model.ClassStatus
import de.yogaknete.app.domain.model.YogaClass
import de.yogaknete.app.domain.worker.ClassNotificationWorker
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes

class ClassNotificationSchedulerImplTest {

    private lateinit var workManager: WorkManager
    private lateinit var clock: Clock
    private lateinit var scheduler: ClassNotificationSchedulerImpl

    private val timeZone = TimeZone.currentSystemDefault()

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
        workManager = mockk(relaxed = true)
        clock = mockk()
    }

    @Test
    fun `schedule enqueues work with correct unique name`() {
        // Set "now" to 1 hour before endTime + 45min
        val now = testClass.endTime.toInstant(timeZone) + 45.minutes - 60.minutes
        every { clock.now() } returns now
        scheduler = ClassNotificationSchedulerImpl(workManager, clock)

        scheduler.schedule(testClass)

        verify {
            workManager.enqueueUniqueWork(
                "class_notification_42",
                ExistingWorkPolicy.REPLACE,
                any<OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun `schedule calculates correct positive delay`() {
        // "now" is 30 minutes before notification time (endTime + 45min)
        val notificationTime = testClass.endTime.toInstant(timeZone) + 45.minutes
        val now = notificationTime - 30.minutes
        every { clock.now() } returns now
        scheduler = ClassNotificationSchedulerImpl(workManager, clock)

        val requestSlot = slot<OneTimeWorkRequest>()

        scheduler.schedule(testClass)

        verify {
            workManager.enqueueUniqueWork(
                "class_notification_42",
                ExistingWorkPolicy.REPLACE,
                capture(requestSlot)
            )
        }

        val workSpec = requestSlot.captured.workSpec
        val delayMs = workSpec.initialDelay
        // Should be approximately 30 minutes (in milliseconds)
        val thirtyMinMs = TimeUnit.MINUTES.toMillis(30)
        assertTrue(
            "Delay should be ~30 minutes but was ${delayMs}ms",
            delayMs in (thirtyMinMs - 1000)..(thirtyMinMs + 1000)
        )
    }

    @Test
    fun `schedule sets delay to 0 for class in the past`() {
        // "now" is 2 hours after endTime + 45min
        val now = testClass.endTime.toInstant(timeZone) + 45.minutes + 120.minutes
        every { clock.now() } returns now
        scheduler = ClassNotificationSchedulerImpl(workManager, clock)

        val requestSlot = slot<OneTimeWorkRequest>()

        scheduler.schedule(testClass)

        verify {
            workManager.enqueueUniqueWork(
                "class_notification_42",
                ExistingWorkPolicy.REPLACE,
                capture(requestSlot)
            )
        }

        val workSpec = requestSlot.captured.workSpec
        assertEquals(0L, workSpec.initialDelay)
    }

    @Test
    fun `cancel calls cancelUniqueWork with correct name`() {
        every { clock.now() } returns Clock.System.now()
        scheduler = ClassNotificationSchedulerImpl(workManager, clock)

        scheduler.cancel(42L)

        verify { workManager.cancelUniqueWork("class_notification_42") }
    }
}
