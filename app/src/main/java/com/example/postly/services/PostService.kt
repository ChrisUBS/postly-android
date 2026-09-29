package com.example.postly.services

import com.example.postly.models.PaginatedPosts
import com.example.postly.models.Pagination
import com.example.postly.models.Post
import com.example.postly.networking.APIClient
import com.example.postly.networking.Endpoint
import org.json.JSONObject

class PostService(private val apiClient: APIClient) {
    suspend fun getAllPosts(page: Int = 1, limit: Int = 10): PaginatedPosts {
        val response = apiClient.request(Endpoint.GetPosts(page, limit))
        val posts = response.optJSONArray("posts")?.let { array -> List(array.length()) { Post.fromJson(array.getJSONObject(it)) } }.orEmpty()
        return PaginatedPosts(posts, Pagination.fromJson(response.optJSONObject("pagination") ?: org.json.JSONObject()))
    }

    suspend fun getPostById(id: String): Post = Post.fromJson(apiClient.request(Endpoint.GetPost(id)))
    suspend fun createPost(title: String, content: String, status: String, coverImage: String?): Post = Post.fromJson(
        apiClient.request(Endpoint.CreatePost, method = "POST", body = postBody(title, content, status, coverImage), requiresAuth = true)
    )
    suspend fun updatePost(id: String, title: String, content: String, status: String, coverImage: String?): Post = Post.fromJson(
        apiClient.request(Endpoint.UpdatePost(id), method = "PUT", body = postBody(title, content, status, coverImage), requiresAuth = true)
    )
    suspend fun getMyPosts(): PaginatedPosts {
        val response = apiClient.request(Endpoint.GetMyPosts, requiresAuth = true)
        val posts = response.optJSONArray("posts")?.let { array -> List(array.length()) { Post.fromJson(array.getJSONObject(it)) } }.orEmpty()
        return PaginatedPosts(posts, Pagination.fromJson(response.optJSONObject("pagination") ?: org.json.JSONObject()))
    }
    suspend fun deletePost(id: String) { apiClient.request(Endpoint.DeletePost(id), method = "DELETE", requiresAuth = true) }
    suspend fun likePost(id: String) { apiClient.request(Endpoint.LikePost(id), method = "POST", requiresAuth = true) }
    suspend fun unlikePost(id: String) { apiClient.request(Endpoint.LikePost(id), method = "DELETE", requiresAuth = true) }
    suspend fun checkLike(id: String): Boolean = apiClient.request(Endpoint.LikePost(id), requiresAuth = true).optBoolean("liked")

    private fun postBody(title: String, content: String, status: String, coverImage: String?): JSONObject = JSONObject()
        .put("title", title).put("content", content).put("status", status).put("coverImage", coverImage ?: JSONObject.NULL)
}
