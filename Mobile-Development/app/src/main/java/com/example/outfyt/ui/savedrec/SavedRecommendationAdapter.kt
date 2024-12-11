package com.example.outfyt.ui.savedrec

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.outfyt.R
import com.example.outfyt.data.local.entity.OutfitEntity
import com.example.outfyt.databinding.SavedRecommendationItemBinding

class SavedRecommendationAdapter(
    private var savedItems: List<OutfitEntity>,
    private val onDeleteClick: (OutfitEntity) -> Unit
) : RecyclerView.Adapter<SavedRecommendationAdapter.SavedRecommendationViewHolder>() {

    class SavedRecommendationViewHolder(private val binding: SavedRecommendationItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val itemImage: ImageView = binding.ivItemImage
        val itemName: TextView = binding.tvItemName
        val btnDelete: ImageButton = binding.btnDelete
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedRecommendationViewHolder {
        val binding = SavedRecommendationItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SavedRecommendationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SavedRecommendationViewHolder, position: Int) {
        val item = savedItems[position]

        holder.itemName.text = item.name

        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.ic_image)
            .into(holder.itemImage)

        holder.btnDelete.setImageResource(R.drawable.ic_save_filled)
        holder.btnDelete.setOnClickListener {
            onDeleteClick(item)
            holder.btnDelete.setImageResource(R.drawable.ic_save_border)
            Toast.makeText(holder.itemView.context, "Item removed from saved recommendations", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = savedItems.size

    fun updateItems(newItems: List<OutfitEntity>) {
        savedItems = newItems
        notifyDataSetChanged()
    }
}