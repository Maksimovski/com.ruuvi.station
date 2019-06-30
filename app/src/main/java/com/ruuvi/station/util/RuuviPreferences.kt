package com.ruuvi.station.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager


class RuuviPreferences(val context: Context) {
    var pref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private companion object{
        const val KEY_MQTT_BROKER_URL = "mqtt_broker_url"
        const val KEY_MQTT_BROKER_USERNAME = "mqtt_broker_username"
        const val KEY_MQTT_BROKER_PASSWORD = "mqtt_broker_password"
    }
    var backgroundScanInterval: Int
        get() = pref.getInt("pref_background_scan_interval", Constants.DEFAULT_SCAN_INTERVAL)
        set(interval) {
            pref.edit().putInt("pref_background_scan_interval", interval).apply()
        }

    var backgroundScanMode: BackgroundScanModes
        get() = BackgroundScanModes.fromInt(pref.getInt("pref_background_scan_mode", BackgroundScanModes.DISABLED.value))!!
        set(mode) {
            pref.edit().putInt("pref_background_scan_mode", mode.value).apply()
        }

    /*
    var backgroundScanEnabled: Boolean
        get() = pref.getBoolean("pref_bgscan", false)
        set(enabled) {
            pref.edit().putBoolean("pref_bgscan", enabled).apply()
        }

    var foregroundServiceEnabled: Boolean
        get() = pref.getBoolean("foreground_service", false)
        set(enabled) {
            pref.edit().putBoolean("foreground_service", enabled).apply()
        }
    */

    var isFirstStart: Boolean
        get() = pref.getBoolean("FIRST_START_PREF", true)
        set(enabled) {
            pref.edit().putBoolean("FIRST_START_PREF", enabled).apply()
        }

    var isFirstGraphVisit: Boolean
        get() = pref.getBoolean("first_graph_visit", true)
        set(enabled) {
            pref.edit().putBoolean("first_graph_visit", enabled).apply()
        }

    var temperatureUnit: String
        get() = pref.getString("pref_temperature_unit", "C")
        set(unit) {
            pref.edit().putString("pref_temperature_unit", unit).apply()
        }

    var gatewayUrl: String
        get() = pref.getString("pref_backend", "")
        set(url) {
            pref.edit().putString("pref_backend", url).apply()
        }

    var deviceId: String
        get() = pref.getString("pref_device_id", "")
        set(id) {
            pref.edit().putString("pref_device_id", id).apply()
        }

    var serviceWakelock: Boolean
        get() = pref.getBoolean("pref_wakelock", false)
        set(enabled) {
            pref.edit().putBoolean("pref_wakelock", enabled).apply()
        }

    var dashboardEnabled: Boolean
        get() = pref.getBoolean("DASHBOARD_ENABLED_PREF", false)
        set(enabled) {
            pref.edit().putBoolean("DASHBOARD_ENABLED_PREF", enabled).apply()
        }

    var batterySaverEnabled: Boolean
        get() = pref.getBoolean("pref_bgscan_battery_saving", false)
        set(enabled) {
            pref.edit().putBoolean("pref_bgscan_battery_saving", enabled).apply()
        }

    var mqttBrokerUrl: String
        get() = pref.getString(KEY_MQTT_BROKER_URL, "tcp://broker.hivemq.com:1883")!!
        set(value) {
            pref.edit().putString(KEY_MQTT_BROKER_URL, value).apply()
        }

    var mqttBrokerUsername: String
        get() = pref.getString(KEY_MQTT_BROKER_USERNAME, "")!!
        set(value) {
            pref.edit().putString(KEY_MQTT_BROKER_USERNAME, value).apply()
        }

    var mqttBrokerPassword: CharArray
        get() = pref.getString(KEY_MQTT_BROKER_PASSWORD, "")!!.toCharArray()
        set(value) {
            pref.edit().putString(KEY_MQTT_BROKER_PASSWORD, String(value)).apply()
        }
}