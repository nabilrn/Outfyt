package com.example.outfyt.ui.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outfyt.R
import com.example.outfyt.data.local.LoginPreferences
import com.example.outfyt.data.remote.response.RefreshTokenRequest
import com.example.outfyt.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _uploadStatus = MutableLiveData<String>()
    val uploadStatus: LiveData<String> get() = _uploadStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val apiService = ApiConfig.api

    fun refreshAccessToken(context: Context, callback: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val accessToken = LoginPreferences.getAccessToken(context)
                val googleId = LoginPreferences.getGoogleId(context)
                val refreshTokenRequest = RefreshTokenRequest(googleId.toString())
                val response = apiService.refreshToken("Bearer $accessToken", refreshTokenRequest)

                Log.d("HomeViewModel", "Token: $accessToken")
                Log.d("HomeViewModel", "Google ID: $googleId")

                when {
                    response.isSuccessful && response.body()?.success == true -> {
                        val tokenResponse = response.body()!!
                        val newAccessToken = tokenResponse.accessToken

                        if (!newAccessToken.isNullOrEmpty()) {
                            LoginPreferences.saveAccessToken(context, newAccessToken)
                            _uploadStatus.postValue(context.getString(R.string.token_refreshed_successfully))
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
        Log.e("HomeViewModel", "Token Refresh Error: $errorMessage")
        _uploadStatus.postValue(context.getString(R.string.token_refresh_failed))
    }
}