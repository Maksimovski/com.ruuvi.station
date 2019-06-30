package com.ruuvi.station.mqtt.alarm

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.ruuvi.station.mqtt.alarm.utils.ExtrasHelper

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(ctx: Context?, intent: Intent?) {
        ctx?.let { context ->
            (context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.ruuvi.station::AlarmReceiver").apply {
                    acquire(2000)
                    val alarmManager = context.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val alarmScheduler = AlarmScheduler(context.applicationContext, alarmManager)
                    intent?.let {
                        val alarm: MqttPublishAlarmJob? = ExtrasHelper.getObject(
                                intent,
                                RuuviAlarmManager.EXTRA_KEY_ALARM
                        )
                        alarm?.let { recentAlarm ->
                            alarmScheduler.schedule(AlarmJobFactory.nextAlarm(recentAlarm))
                        }
                                ?: Log.e(RuuviAlarmManager.TAG, "Unable to re-schedule the alarm because the received alarm is null!")
                    }
                    release()
                }
            }
        }

    }
}