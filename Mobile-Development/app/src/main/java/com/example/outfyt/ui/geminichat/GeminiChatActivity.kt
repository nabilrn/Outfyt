package com.example.outfyt.ui.geminichat

import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.outfyt.R
import com.example.outfyt.data.local.LoginPreferences
import com.example.outfyt.databinding.ActivityGeminiChatBinding

class GeminiChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGeminiChatBinding
    private lateinit var chatAdapter: ChatAdapter

    private val viewModel: GeminiChatViewModel by viewModels()

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
        setupObservers()
        startChatSession()

        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text.toString()
            if (message.isNotEmpty()) {
                sendMessage(message)
                hideKeyboard()
            }
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@GeminiChatActivity).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    private fun setupObservers() {
        viewModel.messages.observe(this) { messages ->
            chatAdapter.submitList(messages)
            if (messages.isNotEmpty()) {
                binding.recyclerView.smoothScrollToPosition(messages.size - 1)
            }
        }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                is GeminiChatViewModel.UiState.Loading -> {
                }
                is GeminiChatViewModel.UiState.Success -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                is GeminiChatViewModel.UiState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun startChatSession() {
        val accessToken = LoginPreferences.getAccessToken(this)
        if (accessToken != null) {
            viewModel.startChatSession(accessToken)
        } else {
            Toast.makeText(this, "Access token not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendMessage(message: String) {
        val accessToken = LoginPreferences.getAccessToken(this)
        if (accessToken != null) {
            viewModel.sendMessage(accessToken, message)
            binding.messageInput.text?.clear()
        } else {
            Toast.makeText(this, "Access token not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.messageInput.windowToken, 0)
    }
}