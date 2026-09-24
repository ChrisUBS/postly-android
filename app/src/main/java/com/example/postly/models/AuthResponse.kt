package com.example.postly.models

import org.json.JSONObject

data class AuthResponse(val accessToken: String, val user: User) {
    companion object {
        fun fromJson(json: JSONObject) = AuthResponse(
            accessToken = json.getString("accessToken"),
            user = User.fromJson(json.getJSONObject("user"))
        )
    }
}
