package com.example.universalyogaapp

import android.content.Context

class SessionManager(context: Context) {
    private var prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREFS_NAME = "YogaAppPrefs"
        const val USER_TOKEN = "user_token"
        const val USER_ID = "user_id"
        const val USER_EMAIL = "user_email"
        const val USER_NAME = "user_name"
    }

    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun saveUserInfo(userId: Int, email: String) {
        val editor = prefs.edit()
        editor.putInt(USER_ID, userId)
        editor.putString(USER_EMAIL, email)
        editor.apply()
    }

    fun saveUserName(name: String) {
        val editor = prefs.edit()
        editor.putString(USER_NAME, name)
        editor.apply()
    }

    fun fetchUserName(): String {
        return prefs.getString(USER_NAME, "User") ?: "User"
    }

    fun fetchUserEmail(): String {
        return prefs.getString(USER_EMAIL, "") ?: ""
    }

    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}
