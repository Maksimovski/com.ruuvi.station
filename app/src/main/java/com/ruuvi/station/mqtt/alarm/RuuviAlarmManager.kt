package com.ruuvi.station.mqtt.alarm

import android.content.Context
import android.content.IntentFilter
import javax.inject.Inject

class RuuviAlarmManager @Inject constructor(
        private var context: Context,
        private var alarmJobFactory: AlarmJobFactory,
        private var alarmReceiver: AlarmReceiver,
        private var alarmScheduler: AlarmScheduler,
        private var alarmExecutor: AlarmExecutor
) {
    init {
        scheduleAlarmJobs()
    }

    private fun scheduleAlarmJobs() {
        scheduleMqttPublishAlarmJob()

        val filter = IntentFilter(ACTION_ALARM)
        context.registerReceiver(alarmExecutor, filter)
        context.registerReceiver(alarmReceiver, filter)
    }

    private fun scheduleMqttPublishAlarmJob() {
        val mqttPublishAlarmJob: MqttPublishAlarmJob =
                alarmJobFactory.createMqttPublishAlarmJob(REPEAT_AFTER_SECONDS)
        alarmScheduler.schedule(mqttPublishAlarmJob)
    }

    companion object {
        const val TAG = "Alarm"
        const val ACTION_ALARM = "com.ruuvi.intent.action.ALARM"
        const val EXTRA_KEY_ALARM = "alarm"
        const val REPEAT_AFTER_SECONDS = 1020 //17min
    }
}