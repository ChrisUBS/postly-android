package com.example.postly.models

import org.json.JSONObject

data class User(
    val userId: String,
    val name: String,
    val email: String?,
    val profilePicture: String?,
    val lastLogin: String?
) {
    companion object {
        fun fromJson(json: JSONObject) = User(
            userId = json.optString("userId"),
            name = json.optString("name", "User"),
            email = json.optNullableString("email"),
            profilePicture = json.optNullableString("profilePicture"),
            lastLogin = json.optNullableString("lastLogin")
        )
    }
}

internal fun JSONObject.optNullableString(name: String): String? =
    if (isNull(name)) null else optString(name).takeIf { it.isNotBlank() }
