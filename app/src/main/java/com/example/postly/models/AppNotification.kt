package com.example.postly.models

import org.json.JSONObject

data class AppNotification(
    val id: String,
    val type: String,
    val actorName: String,
    val postId: String,
    val postTitle: String,
    val createdAt: String,
    val isRead: Boolean
) {
    companion object {
        fun fromJson(json: JSONObject): AppNotification {
            val actor = json.optJSONObject("actor") ?: json.optJSONObject("user") ?: JSONObject()
            val post = json.optJSONObject("post") ?: JSONObject()
            return AppNotification(
                id = json.optString("_id", json.optString("id", json.optString("notificationId"))),
                type = json.optString("type", "comment"),
                actorName = actor.optString("name", json.optString("actorName", "Someone")),
                postId = post.optString("_id", json.optString("postId")),
                postTitle = post.optString("title", json.optString("postTitle", "your post")),
                createdAt = json.optString("createdAt"),
                isRead = json.optBoolean("isRead", json.optBoolean("read", false))
            )
        }
    }
}
