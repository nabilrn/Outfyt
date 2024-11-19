package com.example.outfyt.ui.home

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outfyt.R
import com.example.outfyt.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class HomeViewModel : ViewModel() {
    private val _uploadStatus = MutableLiveData<String>()
    val uploadStatus: LiveData<String> get() = _uploadStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String> get() = _imageUrl

    private val _shouldResetImage = MutableLiveData<Boolean>()
    val shouldResetImage: LiveData<Boolean> get() = _shouldResetImage

    fun uploadImage(token: String, imageUri: Uri, context: Context) {
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

                val response = ApiConfig.api.uploadImage(token, imageMultipart)

                if (response.isSuccessful && response.body() != null) {
                    _uploadStatus.value = context.getString(R.string.upload_successful)
                    _imageUrl.value = response.body()?.url
                    _shouldResetImage.value = true
                } else {
                    _uploadStatus.value = context.getString(R.string.upload_failed, response.message())
                    Log.e("HomeViewModel", "Upload failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _uploadStatus.value = context.getString(R.string.upload_error, e.message)
                Log.e("HomeViewModel", "Upload error", e)
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