package com.example.outfyt.ui.schedule

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.outfyt.data.local.LoginPreferences
import com.example.outfyt.data.remote.retrofit.ApiConfig
import com.example.outfyt.data.remote.response.CalendarResponse
import com.example.outfyt.data.remote.response.EventsItem
import kotlinx.coroutines.launch
import retrofit2.Response

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    val events = MutableLiveData<List<EventsItem>>()
    val errorMessage = MutableLiveData<String>()

    fun fetchCalendarData() {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val accessToken = LoginPreferences.getAccessToken(context) ?: ""

                val response: Response<CalendarResponse> = ApiConfig.api.getCalendar(
                    "Bearer $accessToken"

                )

                Log.d("ScheduleViewModel", "Response: $response")
                Log.d("aksestoken ", accessToken)

                if (response.isSuccessful && response.body() != null) {
                    events.value = response.body()!!.events
                } else {
                    errorMessage.value = "Failed to fetch calendar data"
                }
            } catch (e: Exception) {
                errorMessage.value = "Error fetching calendar data: ${e.message}"
            }
        }
    }
}
