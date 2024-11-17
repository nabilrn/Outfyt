package com.example.outfyt.ui.account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.outfyt.R
import com.example.outfyt.data.local.LoginPreferences
import com.example.outfyt.databinding.FragmentAccountBinding

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!
    private val accountViewModel: AccountViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        accountViewModel.displayName.observe(viewLifecycleOwner, Observer { name ->
            binding.tvWelcome.text = "Welcome, ${name ?: "Guest"}!"
        })

        accountViewModel.logoutSuccess.observe(viewLifecycleOwner, Observer { isLoggedOut ->
            if (isLoggedOut) {
                findNavController().navigate(R.id.action_accountFragment_to_loginFragment)
            }
        })

        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.btnLogout.setOnClickListener {
            logout()
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val displayName = LoginPreferences.getDisplayName(requireContext()) ?: "Guest"
        accountViewModel.setDisplayName(displayName)
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
        accountViewModel.logout(requireContext())
    }
}