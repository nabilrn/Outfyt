package com.example.outfyt.ui.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.outfyt.R
import com.example.outfyt.data.model.ColorRecommendation
import com.example.outfyt.data.model.PersonalColorData
import com.example.outfyt.databinding.FragmentResultBinding

class ResultFragment : Fragment() {
    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!
    private val resultViewModel: ResultViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)

        observeViewModel()
        resultViewModel.fetchPersonalColor(requireContext())
        setupView()
    }

    private fun observeViewModel() {
        resultViewModel.personalColorData.observe(viewLifecycleOwner) { data ->
            updateUI(data)
        }

        resultViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        resultViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(data: PersonalColorData) {
        data.imageUrl?.let { url ->
            Glide.with(requireContext())
                .load(url)
                .into(binding.ivPicture)
        }

        binding.tvColorType.text =
            data.colorType?.replaceFirstChar { it.uppercase() } ?: getString(R.string.not_available)

        binding.tvGenderCategory.text =
            data.genderCategory?.replaceFirstChar { it.uppercase() } ?: getString(R.string.not_available)

        updateRecommendedColors(data.recommendedColors)
    }

    private fun updateRecommendedColors(colors: List<ColorRecommendation>) {
        binding.colorRecommendationContainer.removeAllViews()

        colors.forEach { color ->
            val colorView = layoutInflater.inflate(
                R.layout.item_recommended_color,
                binding.colorRecommendationContainer,
                false
            )

            val colorCircle = colorView.findViewById<View>(R.id.color_circle)
            val hexTextView = colorView.findViewById<TextView>(R.id.hex_text)

            try {
                colorCircle.setBackgroundColor(android.graphics.Color.parseColor(color.hexCode))
                hexTextView.text = color.hexCode
            } catch (e: IllegalArgumentException) {
                hexTextView.text = getString(R.string.invalid_color)
            }

            binding.colorRecommendationContainer.addView(colorView)
        }
    }

    private fun setupView(){
        binding.btnLihatBaju.setOnClickListener {
            findNavController().navigate(R.id.action_resultsFragment_to_homeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}