package com.example.outfyt.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.outfyt.data.remote.retrofit.ApiService
import com.example.outfyt.ui.recommendation.RecommendResultViewModel

class ViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecommendResultViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecommendResultViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}