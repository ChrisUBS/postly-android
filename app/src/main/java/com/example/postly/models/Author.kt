package com.example.postly.models

import org.json.JSONObject

data class Author(val userId: String, val name: String, val profilePicture: String?) {
    companion object {
        fun fromJson(json: JSONObject) = Author(json.optString("userId"), json.optString("name", "Postly user"), json.optNullableString("profilePicture"))
    }
}
