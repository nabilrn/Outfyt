package com.example.outfyt.data.remote.response

import com.google.gson.annotations.SerializedName

data class AuthRequest(
    @SerializedName("idToken") val idToken: String,
    @SerializedName("authCode") val authCode: String
)

data class AuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("user") val user: UserInfo?,
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("refreshToken") val refreshToken: String?,
    @SerializedName("message") val message: String?,
)

data class UserInfo(
    @SerializedName("googleId") val googleId: String,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("email") val email: String,
    @SerializedName("photoUrl") val photoUrl: String
)

data class LogoutRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class LogoutResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)
