package com.multibhasha.di.modules

import android.content.Context
import com.multibhasha.BuildConfig.DEV_URL
import com.multibhasha.di.data.network.ApiInterface
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Module
class NetworkModule @Inject constructor() {


    open fun buildOkHttpClient( app: Context): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .connectTimeout(30L, TimeUnit.SECONDS)
            .writeTimeout(30L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .build()

    @Provides
    @Inject
    @Singleton
    fun provideOkHttpClient(ctx: Context): OkHttpClient = buildOkHttpClient(ctx)

    @Provides
    @Inject
    @Singleton
    @Named("MultiBhasha")
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(DEV_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()

    @Provides
    @Inject
    @Singleton
    fun provideRestApi(@Named("MultiBhasha") retrofit: Retrofit): ApiInterface =
        retrofit.create(ApiInterface::class.java)
}