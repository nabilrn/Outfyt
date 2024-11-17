package com.example.outfyt.ui.home

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.outfyt.data.local.LoginPreferences
import com.example.outfyt.databinding.FragmentHomeBinding
import com.example.outfyt.R

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeViewModel()
        binding.btnUpload.isEnabled = false
    }

    private fun setupViews() {
        binding.btnGallery.setOnClickListener {
            openGallery()
        }

        binding.btnUpload.setOnClickListener {
            if (selectedImageUri == null || !isImageValid(selectedImageUri!!)) {
                Toast.makeText(requireContext(), "Masukkan gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
                binding.btnUpload.isEnabled = false
                return@setOnClickListener
            }
            uploadSelectedImage()
        }

        binding.ivImage.setOnClickListener {
            openGallery()
        }
    }

    private fun observeViewModel() {
        homeViewModel.uploadStatus.observe(viewLifecycleOwner) { status ->
            Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show()
            if (status.startsWith("Upload berhasil")) {
                resetImageState()
            }
        }

        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnUpload.isEnabled = !isLoading && selectedImageUri != null
        }

        homeViewModel.imageUrl.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(url)
                    .into(binding.ivImage)
            }
        }

        homeViewModel.shouldResetImage.observe(viewLifecycleOwner) { shouldReset ->
            if (shouldReset) {
                resetImageState()
                homeViewModel.resetComplete()
            }
        }
    }

    private fun resetImageState() {
        selectedImageUri = null
        binding.ivImage.setImageResource(R.drawable.ic_image)
        binding.btnUpload.isEnabled = false
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null && isImageValid(uri)) {
            selectedImageUri = uri
            binding.ivImage.setImageURI(uri)
            binding.btnUpload.isEnabled = true
        } else {
            selectedImageUri = null
            binding.btnUpload.isEnabled = false
            Toast.makeText(requireContext(), "Gambar tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isImageValid(uri: Uri): Boolean {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            inputStream?.use {
                return it.available() > 0
            } ?: false
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error validating image", e)
            false
        }
    }

    private fun openGallery() {
        getContent.launch("image/*")
    }

    private fun uploadSelectedImage() {
        val currentUri = selectedImageUri
        if (currentUri == null || !isImageValid(currentUri)) {
            Toast.makeText(requireContext(), "Masukkan gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
            binding.btnUpload.isEnabled = false
            return
        }

        val accessToken = LoginPreferences.getAccessToken(requireContext())
        if (accessToken.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        homeViewModel.uploadImage("Bearer $accessToken", currentUri, requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        selectedImageUri = null
        _binding = null
    }
}
