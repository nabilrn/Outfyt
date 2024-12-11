package com.example.outfyt.ui.recommendation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.outfyt.R
import com.example.outfyt.data.local.entity.OutfitEntity
import com.example.outfyt.data.local.room.AppDatabase
import com.example.outfyt.data.remote.response.Item
import com.example.outfyt.data.remote.response.LikeRequest
import com.example.outfyt.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecommendationAdapter(
    private val items: List<Item>,
    private val accessToken: String
) : RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder>() {

    private val savedRecommendationIds = mutableSetOf<String>()

    class RecommendationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemImage: ImageView = view.findViewById(R.id.ivItemImage)
        val itemName: TextView = view.findViewById(R.id.tvItemName)
        val itemLink: TextView = view.findViewById(R.id.tvItemLink)
        val btnLike: ImageButton = view.findViewById(R.id.btnLike)
        val btnSave: ImageButton = view.findViewById(R.id.btnSave)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recommendation_item, parent, false)
        return RecommendationViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        val item = items[position]
        holder.itemName.text = item.name

        holder.itemLink.setText(R.string.visit_link_product)

        holder.itemLink.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.productLink))
                holder.itemView.context.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .into(holder.itemImage)

        updateSaveButtonState(holder, item)

        holder.btnLike.setOnClickListener {
            holder.btnLike.setImageResource(R.drawable.ic_like_filled)
            addLike(holder.itemView.context, item.recommendationId.toString())
        }

        holder.btnSave.setOnClickListener {
            val recommendationEntity = OutfitEntity(
                id = item.recommendationId.toString(),
                name = item.name,
                imageUrl = item.imageUrl,
                productLink = item.productLink
            )

            if (savedRecommendationIds.contains(item.recommendationId.toString())) {
                removeRecommendation(holder.itemView.context, recommendationEntity)
            } else {
                saveRecommendation(holder.itemView.context, recommendationEntity)
            }
        }
    }

    private fun updateSaveButtonState(holder: RecommendationViewHolder, item: Item) {
        val context = holder.itemView.context
        val database = AppDatabase.getDatabase(context)

        CoroutineScope(Dispatchers.IO).launch {
            val savedItem = database.recommendationDao().getRecommendationById(item.recommendationId.toString())

            withContext(Dispatchers.Main) {
                if (savedItem != null) {
                    savedRecommendationIds.add(item.recommendationId.toString())
                    holder.btnSave.setImageResource(R.drawable.ic_save_filled)
                } else {
                    savedRecommendationIds.remove(item.recommendationId.toString())
                    holder.btnSave.setImageResource(R.drawable.ic_save_border)
                }
            }
        }
    }

    private fun saveRecommendation(context: Context, recommendation: OutfitEntity) {
        val database = AppDatabase.getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            database.recommendationDao().insertRecommendation(recommendation)

            withContext(Dispatchers.Main) {
                savedRecommendationIds.add(recommendation.id)
                notifyDataSetChanged()
                Toast.makeText(context, "Recommendation saved", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun removeRecommendation(context: Context, recommendation: OutfitEntity) {
        val database = AppDatabase.getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            database.recommendationDao().deleteRecommendation(recommendation)

            withContext(Dispatchers.Main) {
                savedRecommendationIds.remove(recommendation.id)
                notifyDataSetChanged()
                Toast.makeText(context, "Recommendation removed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addLike(context: Context, recommendationId: String) {
        val apiService = ApiConfig.api
        val likeRequest = LikeRequest(recommendationId)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.addLike("Bearer $accessToken", likeRequest)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // Handle successful response
                    } else {
                        // Handle error response
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun getItemCount(): Int = items.size
}