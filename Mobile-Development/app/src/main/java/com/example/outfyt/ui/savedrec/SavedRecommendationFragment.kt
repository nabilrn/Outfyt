package com.example.outfyt.ui.savedrec

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.outfyt.R
import com.example.outfyt.databinding.FragmentSavedRecommendationBinding

class SavedRecommendationFragment : Fragment() {

    private lateinit var viewModel: SavedRecommendationViewModel
    private lateinit var adapter: SavedRecommendationAdapter
    private var _binding: FragmentSavedRecommendationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedRecommendationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = SavedRecommendationAdapter(emptyList()) { itemToDelete ->
            viewModel.deleteRecommendation(itemToDelete)
        }
        binding.rvSavedRecommendations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSavedRecommendations.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel = ViewModelProvider(this)[SavedRecommendationViewModel::class.java]
        viewModel.savedRecommendations.observe(viewLifecycleOwner) { recommendations ->
            binding.tvEmptySavedState.visibility = if (recommendations.isEmpty()) View.VISIBLE else View.GONE
            adapter.updateItems(recommendations)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
