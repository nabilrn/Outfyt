package com.example.outfyt.data.remote.response

data class AuthRequest(val idToken: String)

data class AuthResponse(
    val success: Boolean,
    val user: UserInfo?
)

data class UserInfo(
    val googleId: String,
    val displayName: String,
    val email: String,
    val photoUrl: String
)
