package com.example.outfyt.ui.recommendation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.outfyt.R
import com.example.outfyt.data.local.LoginPreferences
import com.example.outfyt.data.remote.response.Item
import com.example.outfyt.data.remote.retrofit.ApiConfig
import com.example.outfyt.ui.ViewModelFactory

class RecommendResultFragment : Fragment() {

    private lateinit var viewModel: RecommendResultViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecommendationAdapter
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_recommend_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val apiService = ApiConfig.api
        val factory = ViewModelFactory(apiService)
        viewModel = ViewModelProvider(this, factory)[RecommendResultViewModel::class.java]

        val scheduleId = arguments?.getString("scheduleId")
        val accessToken = LoginPreferences.getAccessToken(requireContext())

        Log.d("RecommendResultFragment", "Schedule ID: $scheduleId")
        Log.d("RecommendResultFragment", "Access Token: $accessToken")

        progressBar = view.findViewById(R.id.progressBar)
        recyclerView = view.findViewById(R.id.rvRecommendations)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        if (scheduleId != null && accessToken != null) {
            showLoading(true)
            viewModel.fetchRecommendations(scheduleId, accessToken)
        } else {
            Log.e("RecommendResultFragment", "Schedule ID or Access Token is null")
        }

        viewModel.recommendations.observe(viewLifecycleOwner, Observer { recommendationResponse ->
            showLoading(false)
            if (recommendationResponse != null) {
                Log.d("RecommendResultFragment", "Received recommendations: $recommendationResponse")
                val items = mutableListOf<Item>()
                recommendationResponse.topwear?.let { items.addAll(it) }
                recommendationResponse.bottomwear?.let { items.addAll(it) }
                recommendationResponse.sandal?.let { items.addAll(it) }
                recommendationResponse.headwear?.let { items.addAll(it) }
                recommendationResponse.flipFlops?.let { items.addAll(it) }
                recommendationResponse.shoes?.let { items.addAll(it) }

                adapter = RecommendationAdapter(items, accessToken.toString())
                recyclerView.adapter = adapter
            } else {
                view.findViewById<TextView>(R.id.tvEmptyState).visibility = View.VISIBLE
                Log.e("RecommendResultFragment", "Recommendation response is null")
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}