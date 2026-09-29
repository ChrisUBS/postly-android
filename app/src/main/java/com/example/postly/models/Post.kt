package com.example.postly.models

import org.json.JSONObject

data class Post(
    val id: String,
    val title: String,
    val content: String,
    val author: Author,
    val createdAt: String,
    val readTime: Int,
    val views: Int,
    val likes: Int,
    val comments: List<Comment>,
    val status: String,
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
            views = json.optInt("views"),
            likes = json.optInt("likes"),
            comments = json.optJSONArray("comments")?.let { array -> List(array.length()) { Comment.fromJson(array.getJSONObject(it)) } }.orEmpty(),
            status = json.optString("status", "published"),
            coverImage = json.optNullableString("coverImage")
        )
    }

    val commentsCount: Int get() = comments.size
}
