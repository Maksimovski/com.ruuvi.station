package com.ruuvi.station.mqtt

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.ruuvi.station.mqtt.alarm.utils.AndroidUtil
import com.ruuvi.station.mqtt.alarm.utils.NotificationFactory
import com.ruuvi.station.util.RuuviPreferences
import dagger.android.AndroidInjection
import javax.inject.Inject

class MqttGatewayService : Service() {

    @Inject
    lateinit var ruuviPreferences: RuuviPreferences

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
        Toast.makeText(ruuviPreferences.context, "Service running", Toast.LENGTH_SHORT).show()
        Log.i(TAG, "Start processing command")

        Log.i(TAG, "Stop processing command")
        commandStopService()
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
