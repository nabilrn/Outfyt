// RecommendationAdapter.kt
package com.example.outfyt.ui.recommendation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.outfyt.R
import com.example.outfyt.data.remote.response.Item

class RecommendationAdapter(private val items: List<Item>) :
    RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder>() {

    class RecommendationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemImage: ImageView = view.findViewById(R.id.ivItemImage)
        val itemName: TextView = view.findViewById(R.id.tvItemName)
        val itemLink: TextView = view.findViewById(R.id.tvItemLink)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recommendation_item, parent, false)
        return RecommendationViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        val item = items[position]
        holder.itemName.text = item.name
        holder.itemLink.text = item.productLink
        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .into(holder.itemImage)
    }

    override fun getItemCount(): Int = items.size
}