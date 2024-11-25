package com.example.outfyt.ui.geminichat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.outfyt.databinding.BubbleChatBinding
import com.example.outfyt.R
import java.time.format.DateTimeFormatter

class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = BubbleChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position), timeFormatter)
    }

    class ChatViewHolder(private val binding: BubbleChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chatMessage: ChatMessage, timeFormatter: DateTimeFormatter) {
            binding.tvMessage.text = chatMessage.message
            binding.tvTimestamp.text = chatMessage.timestamp.format(timeFormatter)

            val layoutParams = binding.cardMessage.layoutParams as ConstraintLayout.LayoutParams

            if (chatMessage.isUserMessage) {
                layoutParams.startToStart = ConstraintLayout.LayoutParams.UNSET
                layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                layoutParams.horizontalBias = 1f
                binding.cardMessage.setCardBackgroundColor(binding.root.context.getColor(R.color.user_message_background))

                binding.cardMessage.apply {
                    shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                        .setTopLeftCornerSize(16f)
                        .setTopRightCornerSize(4f)
                        .setBottomLeftCornerSize(16f)
                        .setBottomRightCornerSize(16f)
                        .build()
                }
            } else {
                layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                layoutParams.endToEnd = ConstraintLayout.LayoutParams.UNSET
                layoutParams.horizontalBias = 0f
                binding.cardMessage.setCardBackgroundColor(binding.root.context.getColor(R.color.gemini_message_background))

                binding.cardMessage.apply {
                    shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                        .setTopLeftCornerSize(4f)
                        .setTopRightCornerSize(16f)
                        .setBottomLeftCornerSize(16f)
                        .setBottomRightCornerSize(16f)
                        .build()
                }
            }

            binding.cardMessage.layoutParams = layoutParams
        }
    }
}

private class ChatDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
    override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
        return oldItem == newItem
    }
}