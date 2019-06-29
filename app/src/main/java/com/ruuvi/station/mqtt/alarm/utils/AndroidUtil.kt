package com.ruuvi.station.mqtt.alarm.utils

import android.os.Build

object AndroidUtil {

    fun isOreoOrHigher() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
}