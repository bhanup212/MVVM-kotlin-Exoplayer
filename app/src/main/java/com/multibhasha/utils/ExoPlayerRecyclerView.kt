package com.multibhasha.utils

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ui.PlayerView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.RequestManager
import com.multibhasha.model.Video
import android.graphics.Point
import android.view.WindowManager
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import android.util.Log
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import android.net.Uri
import android.util.AttributeSet
import android.util.SparseArray
import android.view.ViewGroup
import androidx.core.util.forEach
import androidx.recyclerview.widget.LinearLayoutManager
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsDataSourceFactory
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.multibhasha.view.adapter.VideosAdapter
import com.multibhasha.R
import kotlinx.android.synthetic.main.video_row_item.view.*


class ExoPlayerRecyclerView(var ctx: Context,attr: AttributeSet): RecyclerView(ctx,attr) {

    companion object{
        val TAG = ExoPlayerRecyclerView.javaClass.simpleName
        val Appname = "MultiBhashi"
        const val youtubeUrl = "https://www.youtube.com/watch?v="
    }

    private enum class VolumeState {
        ON, OFF
    }


    private var volumeControl: ImageView?=null
    private var thumbnail: ImageView?=null
    private var progressBar: ProgressBar?=null
    private var viewHolderParent: View?=null
    private var frameLayout: FrameLayout?=null
    private lateinit var videoSurfaceView: PlayerView
    private var videoPlayer: SimpleExoPlayer?=null

    private var videoList:ArrayList<Video> = ArrayList()
    private var videoSurfaceDefaultHeight = 0
    private var screenDefaultHeight = 0
    private var playPosition = -1
    private var isVideoViewAdded: Boolean = false
    //private lateinit var requestManager: RequestManager

    private lateinit var volumeState: VolumeState

    init {
        //View.inflate(context, R.layout.activity_main, this)
        initializeData()
    }
   /* constructor(c:Context,attr: AttributeSet):this(c){
        initializeData()
    }*/

    private fun initializeData(){
        val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val point = Point()
        display.getSize(point)
        videoSurfaceDefaultHeight = point.x
        screenDefaultHeight = point.y

        videoSurfaceView = PlayerView(this.context)
        videoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM)

        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)

        videoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector)

        videoSurfaceView.useController = false
        videoSurfaceView.player = videoPlayer
        setVolumeControl(VolumeState.ON)

        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d(TAG, "onScrollStateChanged: called.")

                    if(thumbnail != null){ // show the old thumbnail
                        thumbnail?.visibility = VISIBLE
                    }
                    // There's a special case when the end of the list has been reached.
                    // Need to handle that with this bit of logic
                    if (!recyclerView.canScrollVertically(1)) {
                        playVideo(true)
                    } else {
                        playVideo(false)
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }
        })


        addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {

            }

            override fun onChildViewDetachedFromWindow(view: View) {
                if (viewHolderParent != null && viewHolderParent!!.equals(view)) {
                    resetVideoView()
                }

            }
        })


        videoPlayer?.addListener(object : Player.EventListener {
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {

            }

            override fun onSeekProcessed() {

            }

            override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {

            }

            override fun onPlayerError(error: ExoPlaybackException?) {
                Log.e(TAG,"onPlayerError: ${error?.message}")
            }

            override fun onLoadingChanged(isLoading: Boolean) {
                Log.e(TAG,"onLoadingChanged: ${isLoading}")
            }

            override fun onPositionDiscontinuity(reason: Int) {

            }

            override fun onRepeatModeChanged(repeatMode: Int) {

            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {

            }

            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {

            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {

                    Player.STATE_BUFFERING -> {

                        Log.e(TAG, "onPlayerStateChanged: Buffering video.")
                        if (progressBar != null) {
                            progressBar?.visibility = VISIBLE
                        }
                    }

                    Player.STATE_ENDED ->{
                        Log.d(TAG, "onPlayerStateChanged: Video ended.");
                        videoPlayer?.seekTo(0)
                    }


                    Player.STATE_READY ->{

                        Log.e(TAG, "onPlayerStateChanged: Ready to play.");
                        if (progressBar != null) {
                            progressBar?.visibility = GONE
                        }
                        if(!isVideoViewAdded){
                            addVideoView()
                        }
                    }

                }
            }

        })
    }

    private fun playVideo(isEndOfList: Boolean) {

        val targetPosition: Int

        if (!isEndOfList) {
            val startPosition = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            var endPosition = (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

            // if there is more than 2 list-items on the screen, set the difference to be 1
            if (endPosition - startPosition > 1) {
                endPosition = startPosition + 1
            }

            // something is wrong. return.
            if (startPosition < 0 || endPosition < 0) {
                return
            }

            // if there is more than 1 list-item on the screen
            if (startPosition != endPosition) {
                val startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition)
                val endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition)

                targetPosition = if (startPositionVideoHeight > endPositionVideoHeight) startPosition else endPosition
            } else {
                targetPosition = startPosition
            }
        } else {
            targetPosition = videoList.size - 1
        }

        Log.d(TAG, "playVideo: target position: $targetPosition")

        // video is already playing so return
        if (targetPosition == playPosition) {
            return
        }

        // set the position of the list-item that is to be played
        playPosition = targetPosition
        if (videoSurfaceView == null) {
            return
        }

        // remove any old surface views from previously playing videos
        videoSurfaceView.visibility = View.INVISIBLE
        removeVideoView(videoSurfaceView)

        val currentPosition = targetPosition - (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

        val child = getChildAt(currentPosition) ?: return

        val holder = child.tag as? VideosAdapter.ViewHolder
        if (holder == null) {
            playPosition = -1
            return
        }

        thumbnail = holder.thumbnail
        progressBar = holder.progressBar
        volumeControl = holder.volumeControl
        viewHolderParent = holder.itemView
        frameLayout = holder.media_container

        videoSurfaceView.player = videoPlayer

        viewHolderParent?.setOnClickListener(videoViewClickListener)

        val dataSourceFactory = DefaultDataSourceFactory(
            context, Util.getUserAgent(context, "MultiBhashi VideoPlayer")
        )
        val videoId = videoList[targetPosition].videoId
        val youTubeUrl = youtubeUrl+videoId
        object :YouTubeExtractor(ctx){
            override fun onExtractionComplete(ytFiles: SparseArray<YtFile>?, videoMeta: VideoMeta?) {
                Log.e(TAG,"youtubevideoExtract: ${ytFiles?.get(22)?.url}")

                val videoSource = ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(ytFiles?.get(22)?.url))

                videoPlayer?.prepare(videoSource)
                videoPlayer?.playWhenReady = true
            }
        }.extract(youTubeUrl,true,true)

        /*val youtubeSource = HlsMediaSource(Uri.parse(youtubeUrl+videoId),dataSourceFactory,null,null)
        val videoSource = ExtractorMediaSource.Factory(dataSourceFactory)
            .createMediaSource(Uri.parse("https://www.youtube.com/watch?v=Jvc0afR-G3E"))

        videoPlayer?.prepare(youtubeSource)
        videoPlayer?.playWhenReady = true*/
    }

    private val videoViewClickListener = OnClickListener { toggleVolume() }

    private fun getVisibleVideoSurfaceHeight(playPosition: Int): Int {
        val at = playPosition - (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        Log.d(TAG, "getVisibleVideoSurfaceHeight: at: $at")

        val child = getChildAt(at) ?: return 0

        val location = IntArray(2)
        child.getLocationInWindow(location)

        return if (location[1] < 0) {
            location[1] + videoSurfaceDefaultHeight
        } else {
            screenDefaultHeight - location[1]
        }
    }

    private fun removeVideoView(videoView:PlayerView) {

        val parent: ViewGroup = videoView.parent as? ViewGroup ?: return

        val index = parent.indexOfChild(videoView)
        if (index >= 0) {
            parent.removeViewAt(index)
            isVideoViewAdded = false
            viewHolderParent?.setOnClickListener(null)
        }

    }

     private fun addVideoView(){
        frameLayout?.addView(videoSurfaceView)
        isVideoViewAdded = true
        videoSurfaceView.requestFocus()
         videoSurfaceView.visibility = VISIBLE
         videoSurfaceView.alpha = 1f
         thumbnail?.visibility = GONE
    }

     private fun resetVideoView(){
        if(isVideoViewAdded){
            removeVideoView(videoSurfaceView)
            playPosition = -1
            videoSurfaceView.visibility = INVISIBLE
            thumbnail?.visibility = GONE
        }
    }

    fun releasePlayer() {

        if (videoPlayer != null) {
            videoPlayer?.release()
            videoPlayer = null
        }

        viewHolderParent = null
    }

     private fun toggleVolume() {
        if (videoPlayer != null) {
            if (volumeState == VolumeState.OFF) {
                Log.d(TAG, "togglePlaybackState: enabling volume.")
                setVolumeControl(VolumeState.ON)

            } else if(volumeState == VolumeState.ON) {
                Log.d(TAG, "togglePlaybackState: disabling volume.")
                setVolumeControl(VolumeState.OFF)

            }
        }
    }

    private fun setVolumeControl(state:VolumeState){
        volumeState = state;
        if(state == VolumeState.OFF){
            videoPlayer?.volume = 0f
            animateVolumeControl()
        }
        else if(state == VolumeState.ON){
            videoPlayer?.volume = 1f
            animateVolumeControl()
        }
    }

    private fun animateVolumeControl(){
        if(volumeControl != null){
            volumeControl?.bringToFront()
            if(volumeState == VolumeState.OFF){
                volumeControl?.setImageResource(R.drawable.ic_volume_off_grey)
            }
            else if(volumeState == VolumeState.ON){
                volumeControl?.setImageResource(R.drawable.ic_volume_up_grey)

            }
            volumeControl?.animate()?.cancel()

            volumeControl?.alpha = 1f

            volumeControl!!.animate()
                .alpha(0f)
                .setDuration(600).startDelay = 1000
        }
    }

    fun setVideoList(videos: ArrayList<Video>) {
        this.videoList = videos
    }
}