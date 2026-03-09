package de.yogaknete.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationChannelManager {

    const val CHANNEL_ID = "class_status_reminder"
    const val CHANNEL_NAME = "Kurs-Erinnerungen"
    const val CHANNEL_DESCRIPTION =
        "Erinnerungen nach Kursende, um den Status zu aktualisieren"

    fun createChannels(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
