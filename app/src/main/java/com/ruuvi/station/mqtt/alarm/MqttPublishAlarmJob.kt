package com.ruuvi.station.mqtt.alarm

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.ruuvi.station.mqtt.MqttGatewayService
import org.joda.time.DateTime
import org.joda.time.DateTimeUtils
import org.joda.time.LocalDate


class MqttPublishAlarmJob(val id: Int,
                          var scheduledDateTime: DateTime,
                          var repeatAfterSeconds: Int) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            DateTime(parcel.readLong()),
            parcel.readInt()
    )

    fun execute(context: Context) {
        MqttGatewayService.startService(context)
    }

    fun nextAlarm(): DateTime {
        var nextTriggerDate = scheduledDateTime.plusSeconds(repeatAfterSeconds)

        if (nextTriggerDate.isBefore(DateTimeUtils.currentTimeMillis())) {
            nextTriggerDate = nextTriggerDate.withDate(LocalDate.now())
        }
        while (nextTriggerDate.isBefore(DateTimeUtils.currentTimeMillis())) {
            nextTriggerDate = nextTriggerDate.withDate(LocalDate.now()).plusSeconds(repeatAfterSeconds)
        }

        return nextTriggerDate
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeLong(scheduledDateTime.millis)
        parcel.writeInt(repeatAfterSeconds)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MqttPublishAlarmJob> {
        override fun createFromParcel(parcel: Parcel): MqttPublishAlarmJob {
            return MqttPublishAlarmJob(parcel)
        }

        override fun newArray(size: Int): Array<MqttPublishAlarmJob?> {
            return arrayOfNulls(size)
        }
    }
}