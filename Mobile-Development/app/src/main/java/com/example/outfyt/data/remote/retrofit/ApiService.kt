package com.example.outfyt.data.remote.retrofit

import com.example.outfyt.data.remote.response.AuthRequest
import com.example.outfyt.data.remote.response.AuthResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("auth/google/android")
    fun authenticate(@Body request: AuthRequest): Call<AuthResponse>
}