package com.ruuvi.station.di.module

import android.app.AlarmManager
import android.content.Context
import com.ruuvi.station.mqtt.alarm.*
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AlarmsModule {

    @Provides
    @Singleton
    fun provideAndroidScheduler(context: Context): AlarmScheduler {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return AlarmScheduler(context, alarmManager)
    }

    @Provides
    @Singleton
    fun provideAlarmReceiver(): AlarmReceiver {
        return AlarmReceiver()
    }

    @Provides
    @Singleton
    fun provideAlarmRunner(): AlarmExecutor {
        return AlarmExecutor()
    }

    @Provides
    @Singleton
    fun provideAlarmJobFactory(): AlarmJobFactory {
        return AlarmJobFactory
    }

    @Provides
    @Singleton
    fun provideAlarmJobManager(
            context: Context,
            alarmFactory: AlarmJobFactory,
            alarmReceiver: AlarmReceiver,
            alarmScheduler: AlarmScheduler,
            alarmExecutor: AlarmExecutor

    ): RuuviAlarmManager {
        return RuuviAlarmManager(
                context,
                alarmFactory,
                alarmReceiver,
                alarmScheduler,
                alarmExecutor
        )
    }
}