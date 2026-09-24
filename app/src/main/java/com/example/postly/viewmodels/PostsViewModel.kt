package com.example.postly.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.postly.models.Post
import com.example.postly.services.PostService

class PostsViewModel(private val postService: PostService) {
    var posts: List<Post> by mutableStateOf(emptyList())
        private set
    var isLoading: Boolean by mutableStateOf(false)
        private set
    var errorMessage: String? by mutableStateOf(null)
        private set
    private var page = 1
    private var totalPages = 1

    suspend fun fetchPosts(reset: Boolean = false) {
        if (isLoading) return
        if (reset) { page = 1; totalPages = 1; posts = emptyList() }
        isLoading = true; errorMessage = null
        try {
            val response = postService.getAllPosts(page)
            posts = if (page == 1) response.posts else posts + response.posts
            totalPages = response.pagination.totalPages
        } catch (error: Exception) {
            errorMessage = error.message ?: "Unable to load posts."
        } finally { isLoading = false }
    }

    suspend fun loadMore() {
        if (page < totalPages && !isLoading) { page += 1; fetchPosts() }
    }

    val canLoadMore: Boolean get() = page < totalPages
}
