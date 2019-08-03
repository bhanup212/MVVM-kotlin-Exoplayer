package com.multibhasha.di.data.network

import com.multibhasha.model.Video
import io.reactivex.Observable
import io.reactivex.Single
import javax.inject.Inject

class NetworkRepository @Inject constructor(val apiInterface: ApiInterface) {

    fun getVideos():Single<ArrayList<Video>>{
        return  apiInterface.getVideoList()
    }
}