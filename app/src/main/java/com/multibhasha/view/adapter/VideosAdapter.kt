package com.multibhasha.view.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.multibhasha.BR
import com.multibhasha.R
import com.multibhasha.model.Video
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.FrameLayout
import android.widget.ImageView


class VideosAdapter: RecyclerView.Adapter<VideosAdapter.ViewHolder>() {

    private var videoList:ArrayList<Video> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideosAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding:ViewDataBinding = DataBindingUtil.inflate(inflater,R.layout.video_row_item,parent,false)

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        Log.d("VideoAdapter:","Videos size is: ${videoList.size}")
        return videoList.size
    }

    override fun onBindViewHolder(holder: VideosAdapter.ViewHolder, position: Int) {
        holder.bindData(videoList[position])
    }
    fun setVideos(list:ArrayList<Video>){
        videoList.clear()
        videoList.addAll(list)
        notifyDataSetChanged()
    }
    inner class ViewHolder(val binding:ViewDataBinding): RecyclerView.ViewHolder(binding.root) {

        var media_container: FrameLayout = binding.root.findViewById(R.id.media_container)
        var title: TextView = binding.root.findViewById(R.id.title)
        var thumbnail:ImageView = binding.root.findViewById(R.id.thumbnail)
        var volumeControl: ImageView = binding.root.findViewById(R.id.volume_control)
        var progressBar: ProgressBar = binding.root.findViewById(R.id.progressBar)
        var parent: View = binding.root

        fun bindData(v:Video){
            parent.tag = this
            binding.setVariable(BR.video,v)
            binding.executePendingBindings()
        }
    }
}