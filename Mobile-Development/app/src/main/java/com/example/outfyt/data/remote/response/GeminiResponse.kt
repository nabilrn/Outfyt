package com.example.outfyt.data.remote.response

import com.google.gson.annotations.SerializedName

data class GenericResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String
)

data class ChatMessageRequest(
    @SerializedName("message") val message: String
)

data class ChatMessageResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: ChatMessageData
)

data class ChatMessageData(
    @SerializedName("reply") val reply: String,
    @SerializedName("promptFeedback") val promptFeedback: String
)