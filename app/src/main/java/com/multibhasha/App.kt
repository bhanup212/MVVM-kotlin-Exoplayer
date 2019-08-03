package com.multibhasha

import android.app.Activity
import android.app.Application
import com.multibhasha.di.components.AppComponent
import com.multibhasha.di.components.DaggerAppComponent
import com.multibhasha.di.modules.ContextModule
import com.multibhasha.di.modules.NetworkModule
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import javax.inject.Inject

class App: Application(), HasActivityInjector {
    override fun activityInjector(): AndroidInjector<Activity>  = activityInjector

    @Inject
    lateinit var activityInjector: DispatchingAndroidInjector<Activity>

    private lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
            .contextModule(ContextModule(applicationContext))
            .networkModule(NetworkModule())
            .build()
        appComponent.inject(this)
    }
    fun getComponent(): AppComponent {
        return  appComponent
    }

}