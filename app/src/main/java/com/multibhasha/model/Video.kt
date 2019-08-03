package com.multibhasha.model

import com.google.gson.annotations.SerializedName


data class Video(

	@field:SerializedName("title")
	val title: String,

	@field:SerializedName("video_id")
	val videoId: String
)