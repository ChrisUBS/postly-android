package com.example.postly.models

import org.json.JSONObject

data class Post(
    val id: String,
    val title: String,
    val content: String,
    val author: Author,
    val createdAt: String,
    val readTime: Int,
    val likes: Int,
    val commentsCount: Int,
    val coverImage: String?
) {
    companion object {
        fun fromJson(json: JSONObject) = Post(
            id = json.optString("_id"),
            title = json.optString("title"),
            content = json.optString("content"),
            author = Author.fromJson(json.optJSONObject("author") ?: JSONObject()),
            createdAt = json.optString("createdAt"),
            readTime = json.optInt("readTime"),
            likes = json.optInt("likes"),
            commentsCount = json.optJSONArray("comments")?.length() ?: json.optInt("comments", 0),
            coverImage = json.optNullableString("coverImage")
        )
    }
}
