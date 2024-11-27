package com.example.outfyt.ui.geminichat

import java.time.LocalDateTime

data class ChatMessage(
    val message: String,
    val isUserMessage: Boolean,
    val timestamp: LocalDateTime = LocalDateTime.now()
)