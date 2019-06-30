package com.ruuvi.station.di.module

import com.ruuvi.station.feature.addtag.AddTagActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector
    abstract fun contributesAddTagActivity(): AddTagActivity
}