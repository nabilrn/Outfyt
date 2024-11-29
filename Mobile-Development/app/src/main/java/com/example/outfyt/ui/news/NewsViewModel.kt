package com.example.outfyt.ui.news

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.outfyt.R
import com.example.outfyt.data.local.LoginPreferences
import com.example.outfyt.data.remote.response.DataItem
import com.example.outfyt.data.remote.response.NewsResponse
import com.example.outfyt.data.remote.response.RefreshTokenRequest
import com.example.outfyt.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.launch
import retrofit2.Response

class NewsViewModel(application: Application): AndroidViewModel(application) {

    private val _uploadStatus = MutableLiveData<String>()
    val uploadStatus: LiveData<String> get() = _uploadStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val apiService = ApiConfig.api

    val news = MutableLiveData<List<DataItem>>()
    val errorMessage = MutableLiveData<String>()

    fun refreshAccessToken(context: Context, callback: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val accessToken = LoginPreferences.getAccessToken(context)
                val googleId = LoginPreferences.getGoogleId(context)
                val refreshTokenRequest = RefreshTokenRequest(googleId.toString())
                val response = apiService.refreshToken("Bearer $accessToken", refreshTokenRequest)

                Log.d("NewsViewModel", "Token: $accessToken")
                Log.d("NewsViewModel", "Google ID: $googleId")

                when {
                    response.isSuccessful && response.body()?.success == true -> {
                        val tokenResponse = response.body()!!
                        val newAccessToken = tokenResponse.accessToken
                        Log.d("NewsViewModel", "New Token: $newAccessToken")

                        if (!newAccessToken.isNullOrEmpty()) {
                            LoginPreferences.saveAccessToken(context, newAccessToken)
                            _isLoading.value = false
                            callback?.invoke(true)
                        } else {
                            handleTokenRefreshFailure(context, "Empty new token")
                            callback?.invoke(false)
                        }
                    }
                    else -> {
                        val errorMessage = response.body()?.error
                            ?: "Refresh failed: ${response.code()} - ${response.message()}"
                        handleTokenRefreshFailure(context, errorMessage)
                        callback?.invoke(false)
                    }
                }
            } catch (e: Exception) {
                handleTokenRefreshFailure(context, "Exception: ${e.message}")
                callback?.invoke(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun handleTokenRefreshFailure(context: Context, errorMessage: String) {
        Log.e("NewsViewModel", "Token Refresh Error: $errorMessage")
        _uploadStatus.postValue(context.getString(R.string.token_refresh_failed))
    }

    fun fetchNewsData() {
        val context = getApplication<Application>().applicationContext
        viewModelScope.launch {
            try {
                val accessToken = LoginPreferences.getAccessToken(context) ?: ""

                val response: Response<NewsResponse> = ApiConfig.api.getNews(
                    "Bearer $accessToken"
                )

                if (response.isSuccessful && response.body() != null) {
                    val newsList = response.body()?.data?.filterNotNull() ?: emptyList()
                    news.value = newsList
                } else if (response.code() == 401) {
                    refreshAccessToken(context) {
                        if (it) {
                            fetchNewsData()
                        } else {
                            errorMessage.value = context.getString(R.string.failed_to_fetch_news_data)
                        }
                    }
                } else {
                    errorMessage.value = context.getString(R.string.failed_to_fetch_news_data)
                }
            } catch (e: Exception) {
                Log.e("NewsViewModel", "Error fetching news: ", e)
                errorMessage.value = context.getString(R.string.error_fetching_news_data, e.message)
            }
        }
    }


}