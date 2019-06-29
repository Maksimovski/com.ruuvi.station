package com.ruuvi.station.di.module

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module

@Module
abstract class AppContext {

    @Binds
    abstract fun application(app: Application): Context
}