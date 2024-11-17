package com.example.outfyt.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.outfyt.R
import com.example.outfyt.databinding.FragmentNewsBinding
import kotlin.getValue

class NewsFragment : Fragment() {

    private lateinit var binding: FragmentNewsBinding
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var recyclerView: RecyclerView
    private val newsViewModel: NewsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewsBinding.inflate(inflater, container, false)

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        newsAdapter = NewsAdapter(mutableListOf())

        newsViewModel.news.observe(viewLifecycleOwner, Observer { newsList ->
            binding.progressBar.visibility = View.GONE
            if (newsList.isNotEmpty()) {
                newsAdapter.updateNewsList(newsList)
                recyclerView.adapter = newsAdapter
            } else {
                binding.tvNews.text = getString(R.string.noNews)
            }
        })

        newsViewModel.errorMessage.observe(viewLifecycleOwner, Observer { errorMessage ->
            binding.tvNews.text = errorMessage
        })

        newsViewModel.fetchNewsData()

        return binding.root
    }
}
