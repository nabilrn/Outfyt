package com.example.outfyt.data.remote.retrofit

import com.example.outfyt.data.remote.response.AuthRequest
import com.example.outfyt.data.remote.response.AuthResponse
import com.example.outfyt.data.remote.response.CalendarResponse
import com.example.outfyt.data.remote.response.ChatMessageRequest
import com.example.outfyt.data.remote.response.ChatMessageResponse
import com.example.outfyt.data.remote.response.GenericResponse
import com.example.outfyt.data.remote.response.LogoutRequest
import com.example.outfyt.data.remote.response.LogoutResponse
import com.example.outfyt.data.remote.response.NewsResponse
import com.example.outfyt.data.remote.response.PersonalColorResponse
import com.example.outfyt.data.remote.response.RefreshTokenRequest
import com.example.outfyt.data.remote.response.TokenResponse
import com.example.outfyt.data.remote.response.UploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @POST("api/auth")
    fun authenticate(@Body request: AuthRequest): Call<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(@Body request: LogoutRequest): Response<LogoutResponse>

    @GET("api/calendar")
    suspend fun getCalendar(
        @Header("Authorization") accessToken: String): Response<CalendarResponse>

    @Multipart
    @POST("api/upload-image")
    suspend fun uploadImage(
        @Header("Authorization") accessToken: String,
        @Part("gender") gender: RequestBody,
        @Part("age") age: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<UploadResponse>

    @GET("api/news")
    suspend fun getNews(
        @Header("Authorization") accessToken: String): Response<NewsResponse>

    @POST("api/chat/start")
    suspend fun startChat(
        @Header("Authorization") accessToken: String
    ): Response<GenericResponse>

    @POST("api/chat/send")
    suspend fun sendMessage(
        @Header("Authorization") accessToken: String,
        @Body message: ChatMessageRequest
    ): Response<ChatMessageResponse>

    @Headers("Content-Type: application/json")
    @POST("api/refresh-token")
    suspend fun refreshToken(
        @Header("Authorization") accessToken: String,
        @Body refreshTokenRequest: RefreshTokenRequest
    ): Response<TokenResponse>

    @GET("api/personal-color")
    suspend fun getPersonalColor(
        @Header("Authorization") accessToken: String
    ): Response<PersonalColorResponse>
//
//    @POST("api/chat/stream")
//    suspend fun streamMessage(
//        @Header("Authorization") accessToken: String,
//        @Body message: ChatMessageRequest
//    ): Response<StreamedEventData>


}