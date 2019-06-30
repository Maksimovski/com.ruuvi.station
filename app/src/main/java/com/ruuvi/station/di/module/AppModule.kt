package com.ruuvi.station.di.module

import android.content.Context
import com.ruuvi.station.mqtt.MqttManager
import com.ruuvi.station.util.RuuviPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(
        includes = [
            AppContext::class,
            AlarmsModule::class
        ]
)
class AppModule {

    @Provides
    fun provideRuuviPreferences(context: Context): RuuviPreferences {
        return RuuviPreferences(context)
    }

    @Singleton
    @Provides
    fun provideMqttManager(context: Context, ruuviPreferences: RuuviPreferences): MqttManager {
        return MqttManager(context, ruuviPreferences)
    }
}