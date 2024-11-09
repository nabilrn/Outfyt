package com.example.outfyt.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.outfyt.R
import com.example.outfyt.data.local.LoginPreferences
import com.example.outfyt.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    private lateinit var googleSignInClient: GoogleSignInClient

    private val signInResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data: Intent? = result.data
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        viewModel.handleSignInResult(task)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            LoginPreferences.isLoggedIn(requireContext()).collect { isLoggedIn ->
                if (isLoggedIn) {
                    val displayName = LoginPreferences.getDisplayName(requireContext())
                    val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment(displayName ?: "")
                    findNavController().navigate(action)
                }
            }
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        binding.signInButton.setOnClickListener {
            signIn()
        }

        viewModel.authResponse.observe(viewLifecycleOwner, Observer { response ->
            if (response?.success == true) {
                lifecycleScope.launch {
                    LoginPreferences.saveLoginState(requireContext(), true, response.user?.displayName)
                    Log.d("SharedPref", "Login state saved")
                }
                val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment(response.user?.displayName ?: "")
                findNavController().navigate(action)
                Log.d("LoginFragment", "Authentication successful")
                Toast.makeText(requireContext(), "Authentication successful", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show()
                Log.e("LoginFragment", "Authentication failed")
            }
        })
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInResultLauncher.launch(signInIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
