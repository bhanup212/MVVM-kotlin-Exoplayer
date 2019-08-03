package com.multibhasha.view.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.multibhasha.App
import com.multibhasha.R
import com.multibhasha.model.Video
import com.multibhasha.utils.ExoPlayerRecyclerView
import com.multibhasha.utils.ViewModelProviderFactory
import com.multibhasha.view.adapter.VideosAdapter
import com.multibhasha.view.viewmodel.MainActivityViewModel
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    private lateinit var mainActivityViewModel: MainActivityViewModel

    @Inject lateinit var factory:ViewModelProviderFactory

    private var videoList:ArrayList<Video> = ArrayList()
    private lateinit var videosAdapter:VideosAdapter
    private lateinit var videosRv:ExoPlayerRecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        (applicationContext as App).getComponent().inject(this)
        mainActivityViewModel = ViewModelProviders.of(this,factory).get(MainActivityViewModel::class.java)

        setUpRecyclerview()
        observeVideos()
    }
    private fun setUpRecyclerview(){
        videosRv = findViewById(R.id.videos_recyclerview)
        videosAdapter = VideosAdapter()

        videosRv.layoutManager = LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        videosRv.itemAnimator = DefaultItemAnimator()
        videosRv.adapter = videosAdapter
        videosAdapter.notifyDataSetChanged()
    }
    private fun observeVideos(){
        mainActivityViewModel.getVideoList().observe(this, Observer {
            videosRv.setVideoList(it)
            videosAdapter.setVideos(it)
        })

        mainActivityViewModel.isLoading().observe(this, Observer {
            if (it){
                progress_bar.visibility = View.VISIBLE
            }else{
                progress_bar.visibility = View.GONE
            }
        })
    }

    override fun onDestroy() {
        videosRv.releasePlayer()
        super.onDestroy()
    }
}
