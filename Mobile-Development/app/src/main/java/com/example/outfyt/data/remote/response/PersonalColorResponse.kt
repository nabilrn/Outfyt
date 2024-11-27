package com.example.outfyt.data.remote.response

import com.google.gson.annotations.SerializedName

data class PersonalColorResponse(

	@field:SerializedName("faceImageUrl")
	val faceImageUrl: String? = null,

	@field:SerializedName("genderCategory")
	val genderCategory: String? = null,

	@field:SerializedName("recommendedColors")
	val recommendedColors: List<String?>? = null,

	@field:SerializedName("colorType")
	val colorType: String? = null
)
