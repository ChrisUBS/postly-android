package com.example.postly.services

import com.example.postly.models.Comment
import com.example.postly.networking.APIClient
import com.example.postly.networking.Endpoint
import org.json.JSONObject

class CommentService(private val apiClient: APIClient) {
    suspend fun createComment(postId: String, content: String): Comment = Comment.fromJson(
        apiClient.request(Endpoint.CreateComment(postId), method = "POST", body = JSONObject().put("content", content), requiresAuth = true)
    )
    suspend fun deleteComment(postId: String, commentId: String) {
        apiClient.request(Endpoint.DeleteComment(postId, commentId), method = "DELETE", requiresAuth = true)
    }
}
