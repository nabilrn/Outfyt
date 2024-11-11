package com.example.outfyt.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.outfyt.databinding.FragmentHomeBinding
import com.example.outfyt.R
import com.example.outfyt.data.local.LoginPreferences

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        homeViewModel.displayName.observe(viewLifecycleOwner, Observer { name ->
            binding.tvWelcome.text = "Welcome, ${name ?: "Guest"}!"
        })

        homeViewModel.logoutSuccess.observe(viewLifecycleOwner, Observer { isLoggedOut ->
            if (isLoggedOut) {
                findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
            }
        })

        binding.btnLogout.setOnClickListener {
            logout()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val displayName = LoginPreferences.getDisplayName(requireContext()) ?: "Guest"
        homeViewModel.setDisplayName(displayName)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun logout() {
        homeViewModel.logout(requireContext())
    }
}



