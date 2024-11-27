package com.example.outfyt.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    }

    private fun setupViews() {
        binding.btnToForm.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_formFragment)
        }

        binding.fab.setOnClickListener {
            val intent = Intent(requireContext(), GeminiChatActivity::class.java)
            startActivity(intent)
        }
    }

}