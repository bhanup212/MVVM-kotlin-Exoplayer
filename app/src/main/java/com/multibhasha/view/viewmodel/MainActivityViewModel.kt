package com.multibhasha.view.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.multibhasha.di.data.network.NetworkRepository
import com.multibhasha.model.Video
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class MainActivityViewModel constructor(val networkRepo:NetworkRepository ) : ViewModel() {

    companion object{
        val TAG = MainActivityViewModel.javaClass.simpleName
    }

    private var isLoading:MutableLiveData<Boolean> = MutableLiveData()
    private var disposable:CompositeDisposable = CompositeDisposable()
    private var videoListLiveData: MutableLiveData<ArrayList<Video>> = MutableLiveData()

    init {
        disposable = CompositeDisposable()
        fetchVideoList()
    }


    fun getVideoList():LiveData<ArrayList<Video>> = videoListLiveData
    fun isLoading():LiveData<Boolean> = isLoading

    fun fetchVideoList(){
        isLoading.value = true
        disposable.add(networkRepo.getVideos()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object: DisposableSingleObserver<ArrayList<Video>>() {
                override fun onSuccess(t: ArrayList<Video>) {
                    videoListLiveData.value?.clear()
                    videoListLiveData.value = t
                    isLoading.value = false

                }

                override fun onError(e: Throwable) {
                    Log.e(TAG,"onError: ${e.message}")
                    isLoading.value = false
                }

            }))
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }

}