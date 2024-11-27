package com.example.outfyt.ui.form

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.outfyt.R
import com.example.outfyt.data.local.LoginPreferences
import com.example.outfyt.databinding.FragmentFormBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class FormFragment : Fragment() {
    private var _binding: FragmentFormBinding? = null
    private val binding get() = _binding!!
    private val formViewModel: FormViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    private var temporaryPhotoUri: Uri? = null
    private var selectedGender: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeViewModel()
        binding.btnUpload.isEnabled = false
        setHasOptionsMenu(true)
        genderDropdown()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                parentFragmentManager.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(requireContext(), getString(R.string.camera_permission_required), Toast.LENGTH_SHORT).show()
            }
        }

    private fun setupViews() {
        binding.btnGallery.setOnClickListener {
            openGallery()
        }

        binding.btnCamera.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
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
        formViewModel.uploadStatus.observe(viewLifecycleOwner) { status ->
            Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show()
            if (status.startsWith(getString(R.string.upload_successful))) {
                resetImageState()
            }
        }

        formViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnUpload.isEnabled = !isLoading && selectedImageUri != null
        }

        formViewModel.imageUrl.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(url)
                    .into(binding.ivImage)
            }
        }

        formViewModel.shouldResetImage.observe(viewLifecycleOwner) { shouldReset ->
            if (shouldReset) {
                resetImageState()
                formViewModel.resetComplete()
            }
        }

        formViewModel.navigateToResults.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                findNavController().navigate(R.id.action_formFragment_to_resultsFragment)
                formViewModel.onNavigatedToResults()
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
            Log.e("FormFragment", "Error validating image", e)
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
            Log.e("FormFragment", "Error starting camera", e)
            Toast.makeText(requireContext(), getString(R.string.failed_to_start_camera), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        getContent.launch("image/*")
    }

    private fun genderDropdown() {
        val genderDropdown: AutoCompleteTextView = binding.genderAutoComplete
        val genderOptions = listOf("Male", "Female")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genderOptions)
        genderDropdown.setAdapter(adapter)
        genderDropdown.setOnItemClickListener { _, _, position, _ ->
            selectedGender = when (position) {
                0 -> "male"
                1 -> "female"
                else -> null
            }
            Log.d("Upload", "Selected gender: $selectedGender")
        }
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

        if (selectedGender.isNullOrBlank()) {
            Toast.makeText(requireContext(), getString(R.string.isi_semua_data), Toast.LENGTH_SHORT).show()
            return
        }

        val age = binding.etAge.text.toString().toIntOrNull()

        if (age == null) {
            Toast.makeText(requireContext(), getString(R.string.isi_semua_data), Toast
                .LENGTH_SHORT)
                .show()
            return
        }

        val googleId = LoginPreferences.getGoogleId(requireContext())

        formViewModel.uploadImage("Bearer $accessToken", selectedGender!!, age, currentUri,
            requireContext
            (), googleId.toString()
        )
        Log.d("FormFragment", "Uploading data with URI: $accessToken, $selectedGender, $age, $currentUri")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        selectedImageUri = null
        _binding = null
    }

}
