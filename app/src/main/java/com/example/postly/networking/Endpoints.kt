package com.example.postly.networking

import java.net.URLEncoder

sealed class Endpoint(val path: String) {
    data object LoginEmail : Endpoint("auth/login/email")
    data object RegisterEmail : Endpoint("auth/register")
    data object CheckAuth : Endpoint("auth/check")
    data class GetPosts(val page: Int, val limit: Int) : Endpoint("posts?page=$page&limit=$limit")
    data class SearchPosts(val query: String) : Endpoint("posts/search?q=${URLEncoder.encode(query, "UTF-8")}")
    data class GetPost(val id: String) : Endpoint("posts/$id")
    data object CreatePost : Endpoint("posts")
    data class UpdatePost(val id: String) : Endpoint("posts/$id")
    data class LikePost(val id: String) : Endpoint("posts/$id/like")
    data class GetComments(val postId: String) : Endpoint("posts/$postId/comments")
    data class CreateComment(val postId: String) : Endpoint("posts/$postId/comments")
    data object GetMyPosts : Endpoint("users/me/posts")
}
