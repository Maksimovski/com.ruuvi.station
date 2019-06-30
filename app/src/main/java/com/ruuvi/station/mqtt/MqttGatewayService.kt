package com.ruuvi.station.mqtt

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.ruuvi.station.model.RuuviTag
import com.ruuvi.station.mqtt.alarm.utils.AndroidUtil
import com.ruuvi.station.mqtt.alarm.utils.NotificationFactory
import com.ruuvi.station.util.RuuviPreferences
import dagger.android.AndroidInjection
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import javax.inject.Inject

class MqttGatewayService : Service() {

    @Inject
    lateinit var ruuviPreferences: RuuviPreferences
    @Inject
    lateinit var mqttManager: MqttManager

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Created")
        if (AndroidUtil.isOreoOrHigher()) {
            Log.i(TAG, "Binding to a notification")
            NotificationFactory.createNotification(this)
        }

        AndroidInjection.inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            processCommand(it)
        }

        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Destroyed")
    }

    private fun processCommand(intent: Intent) {

        if (mqttManager.isConnected()) {
            RuuviTag.getAll(true)
                    .filter { it.isNearbyTag }
                    .forEach { tag ->
                        mqttManager.publish(tag)
                    }
            commandStopService()
        } else {
            mqttManager.connectMqttBroker(object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.i(MqttManager.TAG, "Connection success")
                    RuuviTag.getAll(true)
                            .filter { it.isNearbyTag }
                            .forEach { tag ->
                                mqttManager.publish(tag)
                            }
                    commandStopService()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.i(MqttManager.TAG, "Connection failure: $exception")
                    commandStopService()
                }
            })
        }
    }

    private fun commandStopService() {
        if (AndroidUtil.isOreoOrHigher()) {
            stopForeground(true)
        }
        stopSelf()
        Log.i(TAG, "Command stop service complete")
    }

    companion object {
        const val TAG = "MqttGatewayService"

        fun startService(context: Context) {
            if (AndroidUtil.isOreoOrHigher()) {
                context.startForegroundService(createIntent(context))
            } else {
                context.startService(createIntent(context))
            }
        }

        private fun createIntent(context: Context): Intent {
            return Intent(context, MqttGatewayService::class.java)
        }
    }
}
