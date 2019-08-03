package com.multibhasha.di.data.network

import com.multibhasha.model.Video
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiInterface {

    @GET("/Multibhashi/sample-api/video")
    fun getVideoList(): Single<ArrayList<Video>>
}