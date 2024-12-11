package com.example.outfyt.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recommendations")
data class OutfitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String,
    val productLink: String
)