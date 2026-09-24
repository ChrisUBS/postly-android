package com.example.postly.managers

import android.content.Context

class AuthManager(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("postly_auth", Context.MODE_PRIVATE)
    var accessToken: String?
        get() = preferences.getString("accessToken", null)
        set(value) { preferences.edit().putString("accessToken", value).apply() }

    fun logout() { accessToken = null }
}
