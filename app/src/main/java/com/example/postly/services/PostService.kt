package com.example.postly.services

import com.example.postly.models.PaginatedPosts
import com.example.postly.models.Pagination
import com.example.postly.models.Post
import com.example.postly.networking.APIClient
import com.example.postly.networking.Endpoint

class PostService(private val apiClient: APIClient) {
    suspend fun getAllPosts(page: Int = 1, limit: Int = 10): PaginatedPosts {
        val response = apiClient.request(Endpoint.GetPosts(page, limit))
        val posts = response.optJSONArray("posts")?.let { array -> List(array.length()) { Post.fromJson(array.getJSONObject(it)) } }.orEmpty()
        return PaginatedPosts(posts, Pagination.fromJson(response.optJSONObject("pagination") ?: org.json.JSONObject()))
    }
}
