package com.example.outfyt.data.remote.response

import com.google.gson.annotations.SerializedName

data class UploadResponse(

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("url")
	val url: String
)
