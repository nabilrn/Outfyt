package com.example.outfyt.ui.form

import android.content.Context
import android.net.Uri
import android.util.Log
import android.util.Log.e
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outfyt.R
import com.example.outfyt.data.local.LoginPreferences
import com.example.outfyt.data.remote.response.RefreshTokenRequest
import com.example.outfyt.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class FormViewModel : ViewModel() {
    private val _uploadStatus = MutableLiveData<String>()
    val uploadStatus: LiveData<String> get() = _uploadStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String> get() = _imageUrl

    private val _shouldResetImage = MutableLiveData<Boolean>()
    val shouldResetImage: LiveData<Boolean> get() = _shouldResetImage

    private val _navigateToResults = MutableLiveData<Boolean>()
    val navigateToResults: LiveData<Boolean> get() = _navigateToResults

    private val apiService = ApiConfig.api

    fun onUploadSuccess() {
        _navigateToResults.value = true
    }

    fun onNavigatedToResults() {
        _navigateToResults.value = false
    }

    fun refreshAccessToken(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentAccessToken = LoginPreferences.getAccessToken(context)
                    ?: throw IllegalStateException("No access token available")

                val googleId = LoginPreferences.getGoogleId(context)
                val refreshTokenRequest = RefreshTokenRequest(googleId.toString())
                val response = apiService.refreshToken("Bearer $currentAccessToken", refreshTokenRequest)

                when {
                    response.isSuccessful && response.body()?.success == true -> {
                        val tokenResponse = response.body()!!
                        val newAccessToken = tokenResponse.accessToken

                        if (!newAccessToken.isNullOrEmpty()) {
                            // Save new access token
                            LoginPreferences.saveAccessToken(context, newAccessToken)

                            _uploadStatus.postValue(context.getString(R.string.token_refreshed_successfully))
                            _isLoading.value = false
                        } else {
                            handleTokenRefreshFailure(context, "Empty new token")
                        }
                    }
                    else -> {
                        // Handle specific error scenarios
                        val errorMessage = response.body()?.error
                            ?: "Refresh failed: ${response.code()} - ${response.message()}"
                        handleTokenRefreshFailure(context, errorMessage)
                    }
                }
            } catch (e: Exception) {
                handleTokenRefreshFailure(context, "Exception: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun handleTokenRefreshFailure(context: Context, errorMessage: String) {
        Log.e("FormViewModel", "Token Refresh Error: $errorMessage")
        _uploadStatus.postValue(context.getString(R.string.token_refresh_failed))
    }

    fun uploadImage(token: String, gender: String, age: Int, imageUri: Uri, context: Context, googleId: String) {
        try {
            context.contentResolver.openInputStream(imageUri).use { input ->
                if (input == null || input.available() <= 0) {
                    _uploadStatus.value = context.getString(R.string.invalid_image_file)
                    return
                }
            }
        } catch (_: Exception) {
            _uploadStatus.value = context.getString(R.string.invalid_image_file)
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            var tempFile: File? = null
            try {
                tempFile = createTempFileFromUri(context, imageUri)
                if (!tempFile.exists() || tempFile.length() == 0L) {
                    _uploadStatus.value = context.getString(R.string.empty_or_invalid_image_file)
                    return@launch
                }

                val requestImageFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                    "image",
                    tempFile.name,
                    requestImageFile
                )

                val response = apiService.uploadImage(token, gender, age, imageMultipart)

                if (response.isSuccessful && response.body() != null) {
                    _uploadStatus.value = context.getString(R.string.upload_successful)
                    val imageUrl = response.body()?.url
                    if (imageUrl != null) {
                        _imageUrl.value = imageUrl
                    } else {
                        _uploadStatus.value = context.getString(R.string.upload_failed, "Image URL is null")
                        Log.e("FormViewModel", "Upload failed: Image URL is null")
                    }
                    _shouldResetImage.value = true
                    _isLoading.value = false
                    onUploadSuccess()
                } else if (response.code() == 401) {
                    refreshAccessToken(context)
                    val newToken = LoginPreferences.getAccessToken(context)
                    if (!newToken.isNullOrEmpty()) {
                        uploadImage("Bearer $newToken", gender, age, imageUri, context, googleId)
                    } else {
                        _uploadStatus.postValue(context.getString(R.string.token_refresh_failed))
                    }
                    _shouldResetImage.value = true
                    _isLoading.value = false
                    onUploadSuccess()
                } else {
                    _uploadStatus.value = context.getString(R.string.upload_failed, response.message())
                    Log.e("FormViewModel", "Upload failed: ${response.code()} - ${response.message()}")
                    response.errorBody()?.let {
                        Log.e("FormViewModel", "Error response body: ${it.string()}")
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _uploadStatus.value = context.getString(R.string.upload_error, e.message)
                Log.e("FormViewModel", "Upload error", e)
                _isLoading.value = false
            } finally {
                _isLoading.value = false
                tempFile?.let {
                    if (it.exists()) {
                        it.delete()
                    }
                }
            }
        }
    }

    fun resetComplete() {
        _shouldResetImage.value = false
    }

    private fun createTempFileFromUri(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File.createTempFile("image", ".jpg", context.cacheDir)

        inputStream.use { input ->
            file.outputStream().use { output ->
                input?.copyTo(output)
            }
        }
        return file
    }
}