package com.example.outfyt.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.outfyt.data.remote.response.AuthRequest
import com.example.outfyt.data.remote.response.AuthResponse
import com.example.outfyt.data.remote.retrofit.ApiConfig
import com.google.android.gms.tasks.Task
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {

    private val _authResponse = MutableLiveData<AuthResponse?>()
    val authResponse: LiveData<AuthResponse?> get() = _authResponse

    fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                sendTokenToServer(idToken)
            } else {
                _authResponse.value = null
            }
        } catch (e: ApiException) {
            Log.e("SignIn", "Sign-in failed", e)
            _authResponse.value = null
        }
    }

    private fun sendTokenToServer(idToken: String) {
        val request = AuthRequest(idToken)
        val call = ApiConfig.api.authenticate(request)

        call.enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    _authResponse.value = response.body()
                    Log.d("Auth", "Authentication successful")
                } else {
                    _authResponse.value = null
                    Log.e("Auth", "Authentication failed")
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                _authResponse.value = null
                Log.e("Auth", "Request failed", t)
            }
        })
    }
}
