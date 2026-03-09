package de.yogaknete.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.unmockkConstructor
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class NotificationChannelManagerTest {

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setup() {
        notificationManager = mockk(relaxed = true)
        context = mockk {
            every { getSystemService(Context.NOTIFICATION_SERVICE) } returns notificationManager
        }
        mockkConstructor(NotificationChannel::class)
        every { anyConstructed<NotificationChannel>().description = any() } returns Unit
    }

    @After
    fun tearDown() {
        unmockkConstructor(NotificationChannel::class)
    }

    @Test
    fun `createChannels registers channel with NotificationManager`() {
        NotificationChannelManager.createChannels(context)

        verify { notificationManager.createNotificationChannel(any<NotificationChannel>()) }
    }

    @Test
    fun `createChannels sets correct description on channel`() {
        val descriptionSlot = slot<String>()

        NotificationChannelManager.createChannels(context)

        verify {
            anyConstructed<NotificationChannel>().description = capture(descriptionSlot)
        }
        assertEquals(
            "Erinnerungen nach Kursende, um den Status zu aktualisieren",
            descriptionSlot.captured
        )
    }

    @Test
    fun `CHANNEL_ID is class_status_reminder`() {
        assertEquals("class_status_reminder", NotificationChannelManager.CHANNEL_ID)
    }

    @Test
    fun `CHANNEL_NAME is Kurs-Erinnerungen`() {
        assertEquals("Kurs-Erinnerungen", NotificationChannelManager.CHANNEL_NAME)
    }

    @Test
    fun `CHANNEL_DESCRIPTION is correct German text`() {
        assertEquals(
            "Erinnerungen nach Kursende, um den Status zu aktualisieren",
            NotificationChannelManager.CHANNEL_DESCRIPTION
        )
    }
}
