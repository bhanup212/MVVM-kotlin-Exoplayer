package com.multibhasha.di.components

import com.multibhasha.App
import com.multibhasha.di.modules.ContextModule
import com.multibhasha.di.modules.NetworkModule
import com.multibhasha.di.scopes.MyApplicationScope
import com.multibhasha.view.ui.main.MainActivity
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        NetworkModule::class,
        ContextModule::class]
)
@MyApplicationScope
interface AppComponent {

    fun inject(app:App)
    fun inject(module:MainActivity)
}