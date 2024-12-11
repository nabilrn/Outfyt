package com.example.outfyt.ui.savedrec

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.outfyt.data.local.entity.OutfitEntity
import com.example.outfyt.data.local.room.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SavedRecommendationViewModel(application: Application) : AndroidViewModel(application) {
    private val recommendationDao = AppDatabase.getDatabase(application).recommendationDao()

    val savedRecommendations: LiveData<List<OutfitEntity>> = recommendationDao.getAllRecommendations()

    fun deleteRecommendation(recommendation: OutfitEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            recommendationDao.deleteRecommendation(recommendation)
        }
    }
}