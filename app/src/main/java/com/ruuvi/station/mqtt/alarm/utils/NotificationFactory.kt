package com.ruuvi.station.mqtt.alarm.utils

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import java.util.*

@TargetApi(26)
object NotificationFactory {
    private val ONGOING_NOTIFICATION_ID = getRandomNumber()
    private val CHANNEL_ID = getRandomNumber().toString()

    fun createNotification(context: Service) {
        val channelId = createChannel(context)
        val notification = buildNotification(context, channelId)
        context.startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    private fun buildNotification(context: Service, channelId: String): Notification {
        // Create a notification.
        return Notification.Builder(context, channelId)
                .setStyle(Notification.BigTextStyle())
                .setOnlyAlertOnce(false)
                .build()
    }

    private fun createChannel(context: Service): String {
        // Create a channel.
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelName = "Mqtt publish channel"
        val importance = NotificationManager.IMPORTANCE_LOW
        val notificationChannel = NotificationChannel(CHANNEL_ID, channelName, importance)
        notificationManager.createNotificationChannel(notificationChannel)
        return CHANNEL_ID
    }

    private fun getRandomNumber(): Int {
        return Random().nextInt(100000)
    }
}
