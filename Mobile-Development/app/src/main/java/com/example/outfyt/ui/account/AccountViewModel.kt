package com.example.outfyt.ui.account

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.outfyt.data.local.LoginPreferences
import com.example.outfyt.data.remote.response.LogoutRequest
import com.example.outfyt.data.remote.retrofit.ApiConfig
import kotlinx.coroutines.launch

class AccountViewModel : ViewModel() {
    private val _displayName = MutableLiveData<String?>()
    val displayName: LiveData<String?> = _displayName

    private val _logoutSuccess = MutableLiveData<Boolean>()
    val logoutSuccess: LiveData<Boolean> = _logoutSuccess

    fun setDisplayName(name: String?) {
        _displayName.value = name
    }


    fun logout(context: Context) {
        val currentAccessToken = LoginPreferences.getAccessToken(context)

        if (currentAccessToken == null) {
            val logoutRequest = LogoutRequest(currentAccessToken.toString())
            val apiService = ApiConfig.api

            viewModelScope.launch {
                try {
                    val response = apiService.logout(logoutRequest)

                    if (response.isSuccessful && response.body()?.success == true) {
                        LoginPreferences.saveLoginState(context, false, null, null, null)
                        _logoutSuccess.postValue(true)
                    } else {
                        _logoutSuccess.postValue(false)
                        Log.e("LogoutViewModel", "Logout failed: ${response.message()}")
                    }
                } catch (_: Exception) {
                    _logoutSuccess.postValue(false)
                }
            }
        } else {
            LoginPreferences.saveLoginState(context, false, null, null, null)
            _logoutSuccess.postValue(true)
        }
    }
}