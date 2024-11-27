package com.example.outfyt.ui.geminichat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.outfyt.BuildConfig
import com.example.outfyt.data.remote.response.ChatMessageRequest
import com.example.outfyt.data.remote.retrofit.ApiService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GeminiChatViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService: ApiService = createApiService()

    private val _messages = MutableLiveData<List<ChatMessage>>(emptyList())
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _uiState = MutableLiveData<UiState>(UiState.Idle)
    val uiState: LiveData<UiState> = _uiState

    private fun createApiService(): ApiService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun startChatSession(accessToken: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = apiService.startChat("Bearer $accessToken")
                if (response.isSuccessful) {
                    _uiState.value = UiState.Success(response.body()?.message ?: "Chat session started")
                } else {
                    _uiState.value = UiState.Error("Failed to start chat session")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun sendMessage(accessToken: String, message: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                addMessage(ChatMessage(message, true))

                val request = ChatMessageRequest(message)
                val response = apiService.sendMessage("Bearer $accessToken", request)

                if (response.isSuccessful) {
                    val reply = response.body()?.data?.reply
                    if (reply != null) {
                        addMessage(ChatMessage(reply, false))
                        _uiState.value = UiState.Success("Message sent successfully")
                    } else {
                        _uiState.value = UiState.Error("No response received")
                    }
                } else {
                    _uiState.value = UiState.Error("Failed to send message")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    private fun addMessage(message: ChatMessage) {
        val currentMessages = _messages.value ?: emptyList()
        _messages.value = currentMessages + message
    }

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val message: String) : UiState()
        data class Error(val message: String) : UiState()
    }
}