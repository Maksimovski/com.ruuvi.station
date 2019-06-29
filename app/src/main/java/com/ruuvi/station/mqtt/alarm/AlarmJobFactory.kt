package com.ruuvi.station.mqtt.alarm

import org.joda.time.LocalDateTime

object AlarmJobFactory {
    private const val MQTT_PUBLISH_JOB_ID = 1

    fun nextAlarm(recentAlarm: MqttPublishAlarmJob): MqttPublishAlarmJob {
        return MqttPublishAlarmJob(
                recentAlarm.id,
                recentAlarm.nextAlarm(),
                recentAlarm.repeatAfterSeconds)
    }

    fun createMqttPublishAlarmJob(repeatAfterSeconds: Int): MqttPublishAlarmJob {
        val triggerAt = LocalDateTime.now().plusSeconds(20).toDateTime()
        return MqttPublishAlarmJob(MQTT_PUBLISH_JOB_ID, triggerAt, repeatAfterSeconds)
    }
}