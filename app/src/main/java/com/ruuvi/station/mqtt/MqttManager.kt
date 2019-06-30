package com.ruuvi.station.mqtt

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.ruuvi.station.model.RuuviTag
import com.ruuvi.station.util.RuuviPreferences
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import java.io.UnsupportedEncodingException

class MqttManager(private var context: Context, private var ruuviPreferences: RuuviPreferences) {
    companion object {
        const val TAG = "MQTTManager"
        const val QOS = 1
    }

    private val gson = Gson()
    private var mqttClient: MqttAndroidClient?

    init {
        val clientId = MqttClient.generateClientId()
        Log.i(TAG, "MQTT client id: $clientId")
        mqttClient = MqttAndroidClient(context, ruuviPreferences.mqttBrokerUrl, clientId)
    }

    fun connectMqttBroker(actionCallback: IMqttActionListener) {
        try {
            if (mqttClient == null) {
                val clientId = MqttClient.generateClientId()
                Log.i(TAG, "MQTT client id: $clientId")
                mqttClient = MqttAndroidClient(context, ruuviPreferences.mqttBrokerUrl, clientId)
                connectMqttBroker(actionCallback)
            } else {
                mqttClient?.let {
                    val options = MqttConnectOptions()
                    options.keepAliveInterval = 100
                    options.isAutomaticReconnect = true
                    if (ruuviPreferences.mqttBrokerUsername.isNotEmpty()) {
                        options.userName = ruuviPreferences.mqttBrokerUsername
                        options.password = ruuviPreferences.mqttBrokerPassword
                    }

                    it.connect(options, actionCallback)
                }
            }
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun disconnectMqttBroker(actionCallback: IMqttActionListener) {
        try {
            mqttClient?.disconnect(100, actionCallback)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun subscribeToTopic(topic: String, qos: Int, iMqttMessageListener: IMqttMessageListener) {
        try {
            mqttClient?.subscribe(topic, qos, iMqttMessageListener)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun unSubscribeFromTopic(topic: String, actionCallback: IMqttActionListener) {
        mqttClient?.unsubscribe(topic, context, actionCallback)
    }

    fun publish(tag: RuuviTag) {
        try {
            val tagJson = gson.toJson(gson)
            Log.i(TAG, "Publishing tags: $tagJson \n")
            mqttClient?.publish(tag.id, tagJson.toByteArray(), QOS, true)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun isConnected() = mqttClient?.isConnected ?: false
}