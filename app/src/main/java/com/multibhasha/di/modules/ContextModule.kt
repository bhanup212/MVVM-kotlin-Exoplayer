package com.multibhasha.di.modules

import android.content.Context
import com.multibhasha.di.scopes.MyApplicationScope
import dagger.Module
import dagger.Provides

@Module
class ContextModule(var ctx: Context) {

    @Provides
    @MyApplicationScope
    fun provideContext(): Context {
        return ctx
    }
}