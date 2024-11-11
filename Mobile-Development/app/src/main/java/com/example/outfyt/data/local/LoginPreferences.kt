package com.example.outfyt.data.local

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

object LoginPreferences {

    private const val PREFERENCE_NAME = "user_preferences"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_DISPLAY_NAME = "user_display_name"
    private const val KEY_ACCESS_TOKEN = "access_token"

    fun saveLoginState(context: Context, isLoggedIn: Boolean, displayName: String?, accessToken: String?) {
        val sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        editor.putString(KEY_USER_DISPLAY_NAME, displayName)
        editor.putString(KEY_ACCESS_TOKEN, accessToken)
        editor.apply()

        Log.d("LoginPreferences", "Login state saved: $isLoggedIn, User: $displayName, AccessToken: $accessToken")
    }

    fun isLoggedIn(context: Context): Flow<Boolean> {
        val sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
        return flowOf(isLoggedIn)
    }

    fun getDisplayName(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_USER_DISPLAY_NAME, null)
    }

    fun getAccessToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }
}
