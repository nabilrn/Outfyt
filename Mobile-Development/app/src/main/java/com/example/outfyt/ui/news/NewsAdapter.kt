package com.example.outfyt.ui.news

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.outfyt.data.remote.response.DataItem
import com.example.outfyt.databinding.ItemNewsBinding
import com.example.outfyt.utils.NewsDiffCallback

class NewsAdapter(private val newsList: MutableList<DataItem>) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    inner class NewsViewHolder(private val binding: ItemNewsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(newsItem: DataItem) {
            binding.tvNewsTitle.text = newsItem.title ?: "No Title"
            binding.tvNewsAuthor.text = newsItem.author ?: "Unknown Author"
            binding.tvNewsSynopsis.text = newsItem.synopsis ?: "No Synopsis"
            Glide.with(binding.root).load(newsItem.imageUrl).into(binding.ivNewsImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val newsItem = newsList[position]
        holder.bind(newsItem)
    }

    override fun getItemCount(): Int = newsList.size

    fun updateNewsList(newList: List<DataItem>) {
        val diffCallback = NewsDiffCallback(newsList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        newsList.clear()
        newsList.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }
}
