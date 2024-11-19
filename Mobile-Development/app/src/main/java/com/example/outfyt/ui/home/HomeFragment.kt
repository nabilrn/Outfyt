package com.example.outfyt.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.outfyt.data.local.LoginPreferences
import com.example.outfyt.databinding.FragmentHomeBinding
import com.example.outfyt.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    private var temporaryPhotoUri: Uri? = null

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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), getString(R.string.camera_permission_required), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupViews() {
        binding.btnGallery.setOnClickListener {
            openGallery()
        }

        binding.btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
            } else {
                startCamera()
            }
        }

        binding.btnUpload.setOnClickListener {
            if (selectedImageUri == null || !isImageValid(selectedImageUri!!)) {
                Toast.makeText(requireContext(), getString(R.string.insert_image_first), Toast.LENGTH_SHORT).show()
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
            if (status.startsWith(getString(R.string.upload_successful))) {
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
            Toast.makeText(requireContext(), getString(R.string.invalid_image), Toast.LENGTH_SHORT).show()
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

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            temporaryPhotoUri?.let { uri ->
                selectedImageUri = uri
                binding.ivImage.setImageURI(uri)
                binding.btnUpload.isEnabled = true
            }
        } else {
            Toast.makeText(requireContext(), getString(R.string.failed_to_capture_image), Toast.LENGTH_SHORT).show()
            binding.btnUpload.isEnabled = false
        }
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = requireContext().getExternalFilesDir(null)
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun startCamera() {
        try {
            val photoFile = createImageFile()
            temporaryPhotoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                photoFile
            )
            takePicture.launch(temporaryPhotoUri)
        } catch (e: Exception) {
            Log.e("HomeFragment", "Error starting camera", e)
            Toast.makeText(requireContext(), getString(R.string.failed_to_start_camera), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        getContent.launch("image/*")
    }

    private fun uploadSelectedImage() {
        val currentUri = selectedImageUri
        if (currentUri == null || !isImageValid(currentUri)) {
            Toast.makeText(requireContext(), getString(R.string.insert_image_first), Toast.LENGTH_SHORT).show()
            binding.btnUpload.isEnabled = false
            return
        }

        val accessToken = LoginPreferences.getAccessToken(requireContext())
        if (accessToken.isNullOrBlank()) {
            Toast.makeText(requireContext(), getString(R.string.login_first), Toast.LENGTH_SHORT).show()
            return
        }

        homeViewModel.uploadImage("Bearer $accessToken", currentUri, requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        selectedImageUri = null
        _binding = null
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1
    }
}