package com.example.outfyt.ui.recommendation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        if (scheduleId != null && accessToken != null) {
            viewModel.fetchRecommendations(scheduleId, accessToken)
        }

        recyclerView = view.findViewById(R.id.rvRecommendations)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.recommendations.observe(viewLifecycleOwner, Observer { recommendationResponse ->
            if (recommendationResponse != null) {
                val items = mutableListOf<Item>()
                items.addAll(recommendationResponse.topwear)
                items.addAll(recommendationResponse.bottomwear)
                items.addAll(recommendationResponse.sandal)
                items.addAll(recommendationResponse.headwear)
                items.addAll(recommendationResponse.flipFlops)
                items.addAll(recommendationResponse.shoes)

                adapter = RecommendationAdapter(items)
                recyclerView.adapter = adapter
            } else {
                view.findViewById<TextView>(R.id.tvEmptyState).visibility = View.VISIBLE
            }
        })
    }
}