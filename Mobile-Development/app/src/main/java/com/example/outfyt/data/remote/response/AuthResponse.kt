package com.example.outfyt.data.remote.response

data class AuthRequest(val idToken: String, val authCode: String)

data class AuthResponse(
    val success: Boolean,
    val user: UserInfo?,
    val accessToken: String?,
    val refreshToken: String?,
    val message: String?
)

data class UserInfo(
    val googleId: String,
    val displayName: String,
    val email: String,
    val photoUrl: String
)

data class LogoutRequest(val refreshToken: String)

data class LogoutResponse(val success: Boolean, val message: String)
