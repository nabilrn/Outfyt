package com.example.outfyt.ui.geminichat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.outfyt.BuildConfig
import com.example.outfyt.R
import com.example.outfyt.data.local.LoginPreferences
import com.example.outfyt.data.remote.response.ChatMessageRequest
import com.example.outfyt.data.remote.retrofit.ApiService
import com.example.outfyt.databinding.ActivityGeminiChatBinding
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GeminiChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGeminiChatBinding
    private lateinit var apiService: ApiService
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityGeminiChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupApiService()
        startChatSession()

        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text.toString()
            if (message.isNotEmpty()) {
                sendMessage(message)
            }
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@GeminiChatActivity)
            adapter = chatAdapter
        }
    }

    private fun setupApiService() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    private fun startChatSession() {
        val accessToken = LoginPreferences.getAccessToken(this)
        if (accessToken != null) {
            lifecycleScope.launch {
                try {
                    val response = apiService.startChat("Bearer $accessToken")
                    if (response.isSuccessful) {
                        Toast.makeText(this@GeminiChatActivity, response.body()?.message, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@GeminiChatActivity, "Failed to start chat session", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@GeminiChatActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Access token not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendMessage(message: String) {
        val accessToken = LoginPreferences.getAccessToken(this)
        if (accessToken != null) {
            lifecycleScope.launch {
                try {
                    val request = ChatMessageRequest(message)
                    val response = apiService.sendMessage("Bearer $accessToken", request)
                    if (response.isSuccessful) {
                        val reply = response.body()?.data?.reply
                        chatAdapter.addMessage("You: $message")
                        chatAdapter.addMessage("Gemini: $reply")
                        binding.messageInput.text?.clear()
                    } else {
                        Toast.makeText(this@GeminiChatActivity, "Failed to send message", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@GeminiChatActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Access token not found", Toast.LENGTH_SHORT).show()
        }
    }
}