package com.multibhasha.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.multibhasha.di.data.network.NetworkRepository
import com.multibhasha.view.viewmodel.MainActivityViewModel
import javax.inject.Inject

class ViewModelProviderFactory @Inject constructor(
    private var networkRepository: NetworkRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(MainActivityViewModel::class.java) -> return MainActivityViewModel(
                networkRepository
            ) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }
}