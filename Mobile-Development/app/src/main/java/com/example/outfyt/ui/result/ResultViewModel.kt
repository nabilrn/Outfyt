package com.example.outfyt.ui.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outfyt.data.local.LoginPreferences
import com.example.outfyt.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.launch
import android.content.Context
import android.util.Log
import com.example.outfyt.data.model.ColorRecommendation
import com.example.outfyt.data.model.PersonalColorData

class ResultViewModel : ViewModel() {
    private val _personalColorData = MutableLiveData<PersonalColorData>()
    val personalColorData: LiveData<PersonalColorData> = _personalColorData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val apiService = ApiConfig.api

    fun fetchPersonalColor(context: Context) {
        val accessToken = LoginPreferences.getAccessToken(context)
        if (accessToken.isNullOrBlank()) {
            _errorMessage.value = "No access token available"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getPersonalColor("Bearer $accessToken")

                if (response.isSuccessful) {
                    val personalColorResponse = response.body()
                    personalColorResponse?.let { colorData ->
                        _personalColorData.value = PersonalColorData(
                            imageUrl = colorData.faceImageUrl,
                            colorType = colorData.colorType,
                            genderCategory = colorData.genderCategory,
                            recommendedColors = colorData.recommendedColors?.map {
                                ColorRecommendation(it.toString())
                            }?: emptyList()
                        )
                    }
                } else {
                    _errorMessage.value = "Failed to fetch personal color: ${response.message()}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                Log.e("ResultViewModel", "Error fetching personal color", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}