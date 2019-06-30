package com.ruuvi.station.mqtt.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.ruuvi.station.mqtt.alarm.utils.ExtrasHelper

class AlarmExecutor : BroadcastReceiver() {

    override fun onReceive(ctx: Context?, intent: Intent?) {
        ctx?.let { context ->
            (context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.ruuvi.station::AlarmExecutor").apply {
                    acquire(2000)
                    intent?.let {
                        var alarm: MqttPublishAlarmJob? = ExtrasHelper.getObject(
                                intent,
                                RuuviAlarmManager.EXTRA_KEY_ALARM
                        )
                        alarm?.execute(context.applicationContext)
                    }
                    release()
                }
            }
        }
    }
}