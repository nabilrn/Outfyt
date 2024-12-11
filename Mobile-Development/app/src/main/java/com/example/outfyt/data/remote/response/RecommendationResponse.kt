package com.example.outfyt.data.remote.response

import com.google.gson.annotations.SerializedName

data class RecommendationResponse(

	@field:SerializedName("Bottomwear")
	val bottomwear: List<Item>,

	@field:SerializedName("Sandal")
	val sandal: List<Item>,

	@field:SerializedName("Topwear")
	val topwear: List<Item>,

	@field:SerializedName("Headwear")
	val headwear: List<Item>,

	@field:SerializedName("Flip Flops")
	val flipFlops: List<Item>,

	@field:SerializedName("Shoes")
	val shoes: List<Item>
)

data class Item(

	@field:SerializedName("imageUrl")
	val imageUrl: String,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("productLink")
	val productLink: String,

	@field:SerializedName("recommendationId")
	val recommendationId: Int

)

data class RecommendationRequest(
	@field:SerializedName("scheduleId")
	val scheduleId: String
)

