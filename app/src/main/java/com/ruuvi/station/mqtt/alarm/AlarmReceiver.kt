package com.ruuvi.station.mqtt.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ruuvi.station.mqtt.alarm.utils.ExtrasHelper

class AlarmReceiver(private var scheduler: AlarmScheduler) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            val alarm: MqttPublishAlarmJob? = ExtrasHelper.getObject(
                    intent,
                    RuuviAlarmManager.EXTRA_KEY_ALARM
            )
            alarm?.let { recentAlarm ->
                scheduler.schedule(AlarmJobFactory.nextAlarm(recentAlarm))
            }
                    ?: Log.e(RuuviAlarmManager.TAG, "Unable to re-schedule the alarm because the received alarm is null!")
        }
    }
}