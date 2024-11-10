package com.example.outfyt.ui.splash

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.outfyt.R
import com.example.outfyt.data.local.LoginPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                delay(3000)

                if (!isAdded) return@launch

                val isLoggedIn = LoginPreferences.isLoggedIn(requireContext()).first()
                val displayName = LoginPreferences.getDisplayName(requireContext())

                val action = if (isLoggedIn) {
                    SplashFragmentDirections.actionSplashFragmentToHomeFragment(displayName ?: "Guest")
                } else {
                    SplashFragmentDirections.actionSplashFragmentToLoginFragment()
                }

                findNavController().navigate(action)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
