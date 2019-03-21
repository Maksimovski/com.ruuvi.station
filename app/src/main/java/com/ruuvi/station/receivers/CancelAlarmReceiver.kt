package com.ruuvi.station.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ruuvi.station.model.Alarm


class CancelAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("alarmId", -1)
        val notificationId = intent.getIntExtra("notificationId", -1)
        if (alarmId != -1) {
            val alarm = Alarm.get(alarmId)
            if (alarm != null) {
                alarm.enabled = false
                alarm.update()
            }
        }
        if (notificationId != -1) {
            val ns = Context.NOTIFICATION_SERVICE
            val nMgr = context.getSystemService(ns) as NotificationManager
            nMgr.cancel(notificationId)
        }
    }
}