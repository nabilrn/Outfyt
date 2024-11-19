package com.example.outfyt.ui.news

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.outfyt.R
import com.example.outfyt.data.local.LoginPreferences
import com.example.outfyt.data.remote.response.DataItem
import com.example.outfyt.data.remote.response.NewsResponse
import com.example.outfyt.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.launch
import retrofit2.Response

class NewsViewModel(application: Application): AndroidViewModel(application) {

    val news = MutableLiveData<List<DataItem>>()
    val errorMessage = MutableLiveData<String>()

    fun fetchNewsData() {
        val context = getApplication<Application>().applicationContext
        viewModelScope.launch {
            try {
                val accessToken = LoginPreferences.getAccessToken(context) ?: ""

                val response: Response<NewsResponse> = ApiConfig.api.getNews(
                    "Bearer $accessToken"
                )

                if (response.isSuccessful && response.body() != null) {
                    val newsList = response.body()?.data?.filterNotNull() ?: emptyList()
                    news.value = newsList
                } else {
                    errorMessage.value = context.getString(R.string.failed_to_fetch_news_data)
                }
            } catch (e: Exception) {
                Log.e("NewsViewModel", "Error fetching news: ", e)
                errorMessage.value = context.getString(R.string.error_fetching_news_data, e.message)
            }
        }
    }
}