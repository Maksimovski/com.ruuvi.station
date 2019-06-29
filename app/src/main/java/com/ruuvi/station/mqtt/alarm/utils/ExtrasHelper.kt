package com.ruuvi.station.mqtt.alarm.utils

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable

object ExtrasHelper {

    fun putObject(intent: Intent, key: String, data: Parcelable) {
        val bundle = Bundle()
        bundle.putParcelable(key, data)
        intent.putExtra("extra_bundle", bundle)
    }

    inline fun <reified T : Parcelable> getObject(intent: Intent, key: String): T? {
        val o: Parcelable?

        val bundle = intent.getBundleExtra("extra_bundle") ?: return null

        o = bundle.getParcelable<T>(key)
        if (o == null) {
            return null
        }
        try {
            return o
        } catch (e: ClassCastException) {
            return null
        }

    }
}