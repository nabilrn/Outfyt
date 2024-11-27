package com.example.outfyt.data.remote.response

import com.google.gson.annotations.SerializedName

data class TokenResponse(

	@field:SerializedName("expiryDate")
	val expiryDate: Long? = null,

	@field:SerializedName("success")
	val success: Boolean? = null,

	@field:SerializedName("accessToken")
	val accessToken: String? = null,

	@field:SerializedName("error")
	val error: String? = null
)

data class RefreshTokenRequest(
	@SerializedName("googleId") val googleId: String
)
