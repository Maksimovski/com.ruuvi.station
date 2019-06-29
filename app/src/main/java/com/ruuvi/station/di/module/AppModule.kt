package com.ruuvi.station.di.module

import android.content.Context
import com.ruuvi.station.util.RuuviPreferences
import dagger.Module
import dagger.Provides

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
}