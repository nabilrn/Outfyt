package com.example.outfyt.ui.recommendation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outfyt.data.remote.response.RecommendationRequest
import com.example.outfyt.data.remote.response.RecommendationResponse
import com.example.outfyt.data.remote.retrofit.ApiService
import kotlinx.coroutines.launch

class RecommendResultViewModel(private val apiService: ApiService) : ViewModel() {

    private val _recommendations = MutableLiveData<RecommendationResponse>()
    val recommendations: LiveData<RecommendationResponse> get() = _recommendations

    fun fetchRecommendations(scheduleId: String, accessToken: String) {
        viewModelScope.launch {
            try {
                Log.d("RecommendResultViewModel", "Access Token: $accessToken")
                val response = apiService.getRecommendation("Bearer $accessToken", RecommendationRequest(scheduleId))
                if (response.isSuccessful) {
                    _recommendations.postValue(response.body())
                } else {
                    Log.e("RecommendResultViewModel", "Error fetching recommendations: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("RecommendResultViewModel", "Exception fetching recommendations", e)
            }
        }
    }
}