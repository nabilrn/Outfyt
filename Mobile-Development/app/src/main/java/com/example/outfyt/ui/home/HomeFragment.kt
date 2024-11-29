package com.example.outfyt.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.outfyt.databinding.FragmentHomeBinding
import com.example.outfyt.R
import com.example.outfyt.ui.geminichat.GeminiChatActivity

class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeViewModel()
    }

    private fun observeViewModel() {
        homeViewModel.uploadStatus.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun setupViews() {
        binding.btnToForm.setOnClickListener {
            homeViewModel.refreshAccessToken(requireContext()) { success ->
                if (success) {
                    findNavController().navigate(R.id.action_homeFragment_to_formFragment)
                } else {
                    Toast.makeText(requireContext(),
                        getString(R.string.token_refresh_failed),
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.fab.setOnClickListener {
            val intent = Intent(requireContext(), GeminiChatActivity::class.java)
            startActivity(intent)
        }
    }

}