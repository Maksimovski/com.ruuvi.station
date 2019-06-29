package com.ruuvi.station.mqtt.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ruuvi.station.mqtt.alarm.utils.ExtrasHelper

class AlarmExecutor : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            var alarm: MqttPublishAlarmJob? = ExtrasHelper.getObject(
                    intent,
                    RuuviAlarmManager.EXTRA_KEY_ALARM
            )
            context?.let {
                alarm?.execute(it)
            }
        }
    }
}