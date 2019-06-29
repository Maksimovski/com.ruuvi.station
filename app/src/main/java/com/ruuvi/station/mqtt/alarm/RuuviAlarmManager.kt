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
        val repeatEverySeconds = 20
        val mqttPublishAlarmJob: MqttPublishAlarmJob =
                alarmJobFactory.createMqttPublishAlarmJob(repeatEverySeconds)
        alarmScheduler.schedule(mqttPublishAlarmJob)
    }

    companion object {
        const val TAG = "Alarm"
        const val ACTION_ALARM = "com.ruuvi.intent.action.ALARM"
        const val EXTRA_KEY_ALARM = "alarm"
    }
}