package com.example.postly.models

import org.json.JSONObject

data class Comment(val id: String, val content: String, val author: Author, val createdAt: String) {
    companion object {
        fun fromJson(json: JSONObject) = Comment(
            id = json.optString("_id"),
            content = json.optString("content"),
            author = Author.fromJson(json.optJSONObject("author") ?: JSONObject()),
            createdAt = json.optString("createdAt")
        )
    }
}
