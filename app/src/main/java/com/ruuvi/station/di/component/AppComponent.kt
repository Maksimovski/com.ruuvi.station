package com.ruuvi.station.di.component

import android.app.Application
import com.ruuvi.station.RuuviScannerApplication
import com.ruuvi.station.di.module.AppModule
import com.ruuvi.station.di.module.ServiceBuilder
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
        modules = [
            AndroidInjectionModule::class,
            AppModule::class,
            ServiceBuilder::class
        ]
)
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(app: RuuviScannerApplication)
}