package com.multibhasha.di.modules

import com.multibhasha.view.ui.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BuildersModule {

    //@ContributesAndroidInjector
    abstract fun contributeMainActivity(): MainActivity
}