package com.example.outfyt.ui.schedule

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.outfyt.R
import com.example.outfyt.data.local.LoginPreferences
import com.example.outfyt.data.remote.response.CalendarResponse
import com.example.outfyt.data.remote.response.EventsItem
import com.example.outfyt.data.remote.response.RefreshTokenRequest
import com.example.outfyt.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.launch
import retrofit2.Response

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val _uploadStatus = MutableLiveData<String>()
    val uploadStatus: LiveData<String> get() = _uploadStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    val events = MutableLiveData<List<EventsItem>>()
    val errorMessage = MutableLiveData<String>()

    private val apiService = ApiConfig.api

    fun refreshAccessToken(context: Context, callback: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val accessToken = LoginPreferences.getAccessToken(context)
                val googleId = LoginPreferences.getGoogleId(context)
                val refreshTokenRequest = RefreshTokenRequest(googleId.toString())
                val response = apiService.refreshToken("Bearer $accessToken", refreshTokenRequest)

                when {
                    response.isSuccessful && response.body()?.success == true -> {
                        val tokenResponse = response.body()!!
                        val newAccessToken = tokenResponse.accessToken
                        Log.d("ScheduleViewModel", "New Token: $newAccessToken")

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
        Log.e("ScheduleViewModel", "Token Refresh Error: $errorMessage")
        _uploadStatus.postValue(context.getString(R.string.token_refresh_failed))
    }

    fun fetchCalendarData() {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val accessToken = LoginPreferences.getAccessToken(context) ?: ""
                val googleId = LoginPreferences.getGoogleId(context)

                Log.d("ScheduleViewModel", "Fetching calendar data with access token: $accessToken and googleId: $googleId")

                val response: Response<CalendarResponse> = ApiConfig.api.getCalendar(
                    "Bearer $accessToken"
                )

                if (response.isSuccessful && response.body() != null) {
                    Log.d("ScheduleViewModel", "Successfully fetched calendar data")
                    events.value = response.body()!!.events
                } else if (response.code() == 401) {
                    Log.d("ScheduleViewModel", "Access token expired, refreshing token")
                    refreshAccessToken(context) {
                        if (it) {
                            Log.d("ScheduleViewModel", "Token refreshed successfully, retrying fetch")
                            fetchCalendarData()
                        } else {
                            Log.e("ScheduleViewModel", "Failed to refresh token")
                            errorMessage.value = context.getString(R.string.failed_to_fetch_calendar_data)
                        }
                    }
                } else {
                    Log.e("ScheduleViewModel", "Failed to fetch calendar data: ${response.code()} - ${response.message()}")
                    errorMessage.value = context.getString(R.string.failed_to_fetch_calendar_data)
                }
            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error fetching calendar data: ${e.message}")
                errorMessage.value = "Error fetching calendar data: ${e.message}"
            }
        }
    }

}

