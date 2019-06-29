package com.ruuvi.station.mqtt.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.ruuvi.station.mqtt.alarm.utils.ExtrasHelper
import org.joda.time.format.DateTimeFormat

class AlarmScheduler(private var context: Context, private var alarmManager: AlarmManager) {

    private val timestampFormatter = DateTimeFormat.forPattern("dd-MM-yyyy HH:mm:ss")

    fun schedule(alarm: MqttPublishAlarmJob) {
        val pendingIntent = createAlarmPendingIntent(alarm)

        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarm.scheduledDateTime.millis,
                    pendingIntent
            )
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarm.scheduledDateTime.millis,
                    pendingIntent
            )
            else -> alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    alarm.scheduledDateTime.millis,
                    pendingIntent
            )
        }
        Log.i(
                RuuviAlarmManager.TAG, String.format(
                "Alarm#%d: '%s' will be triggered at %s",
                alarm.id,
                alarm::class.java,
                timestampFormatter.print(alarm.scheduledDateTime.millis))
        )
    }

    private fun createAlarmPendingIntent(alarm: MqttPublishAlarmJob): PendingIntent {
        val intent = Intent(RuuviAlarmManager.ACTION_ALARM)
        ExtrasHelper.putObject(intent, RuuviAlarmManager.EXTRA_KEY_ALARM, alarm)
        return PendingIntent.getBroadcast(
                context,
                alarm.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}