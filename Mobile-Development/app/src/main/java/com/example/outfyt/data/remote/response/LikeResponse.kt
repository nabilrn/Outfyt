package com.example.outfyt.data.remote.response

import com.google.gson.annotations.SerializedName

data class LikeResponse(

	@field:SerializedName("data")
	val data: String? = null,

	@field:SerializedName("message")
	val message: String? = null
)
data class LikeRequest(
	@SerializedName("recommendationId") val recommendationId: String
)