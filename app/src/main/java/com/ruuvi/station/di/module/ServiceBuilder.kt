package com.ruuvi.station.di.module

import com.ruuvi.station.mqtt.MqttGatewayService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilder {

    @ContributesAndroidInjector
    abstract fun contributesMqttGatewayService(): MqttGatewayService
}