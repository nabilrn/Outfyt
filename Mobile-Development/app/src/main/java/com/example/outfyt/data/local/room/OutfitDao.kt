package com.example.outfyt.data.local.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.outfyt.data.local.entity.OutfitEntity

@Dao
interface OutfitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendation(recommendation: OutfitEntity)

    @Query("SELECT * FROM recommendations")
    fun getAllRecommendations(): LiveData<List<OutfitEntity>>

    @Delete
    suspend fun deleteRecommendation(recommendation: OutfitEntity)

    @Query("SELECT * FROM recommendations WHERE id = :recommendationId")
    suspend fun getRecommendationById(recommendationId: String): OutfitEntity?
}