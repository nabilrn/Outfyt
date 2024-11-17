package com.example.outfyt.data.remote.response

import com.google.gson.annotations.SerializedName

data class NewsResponse(

	@field:SerializedName("data")
	val data: List<DataItem?>? = null,

	@field:SerializedName("count")
	val count: Int? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class DataItem(

	@field:SerializedName("author")
	val author: String? = null,

	@field:SerializedName("imageUrl")
	val imageUrl: String? = null,

	@field:SerializedName("link")
	val link: String? = null,

	@field:SerializedName("synopsis")
	val synopsis: String? = null,

	@field:SerializedName("title")
	val title: String? = null
)
