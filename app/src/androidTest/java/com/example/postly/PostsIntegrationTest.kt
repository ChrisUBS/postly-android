package com.example.postly

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.postly.networking.ApiRequester
import com.example.postly.networking.Endpoint
import com.example.postly.services.PostService
import com.example.postly.ui.theme.PostlyTheme
import com.example.postly.ui.views.posts.PostsView
import com.example.postly.viewmodels.PostsViewModel
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PostsIntegrationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun postsScreen_displaysPostsReturnedByApi() {
        val requester = FakePostsApi().apply {
            responses += postsResponse(postJson("post-1", "API title", "Content from the API"))
        }
        val viewModel = PostsViewModel(PostService(requester))

        composeRule.setContent {
            PostlyTheme {
                PostsView(viewModel = viewModel, onPostClick = {})
            }
        }

        composeRule.onNodeWithText("API title").assertExists()
        composeRule.onNodeWithText("Content from the API").assertExists()
        assertEquals("posts?page=1&limit=10", requester.calls.single().path)
        assertEquals("GET", requester.calls.single().method)
    }

    @Test
    fun postsScreen_retryAfterFailedRequest_showsPosts() {
        val requester = FakePostsApi().apply {
            failuresRemaining = 1
            responses += postsResponse(postJson("post-2", "Recovered post", "Loaded after retry"))
        }
        val viewModel = PostsViewModel(PostService(requester))

        composeRule.setContent {
            PostlyTheme {
                PostsView(viewModel = viewModel, onPostClick = {})
            }
        }

        composeRule.onNodeWithText("Service unavailable").assertExists()
        composeRule.onNodeWithText("Retry").performClick()
        composeRule.onNodeWithText("Recovered post").assertExists()
        composeRule.onNodeWithText("Service unavailable").assertDoesNotExist()
        assertEquals(2, requester.calls.size)
    }

    @Test
    fun postsScreen_clickingPostPassesItsIdToNavigation() {
        val requester = FakePostsApi().apply {
            responses += postsResponse(postJson("post-42", "Open this post", "Tap to open"))
        }
        val viewModel = PostsViewModel(PostService(requester))
        var selectedPostId: String? = null

        composeRule.setContent {
            PostlyTheme {
                PostsView(viewModel = viewModel, onPostClick = { selectedPostId = it })
            }
        }

        composeRule.onNodeWithText("Open this post").performClick()
        composeRule.runOnIdle { assertEquals("post-42", selectedPostId) }
    }

    private class FakePostsApi : ApiRequester {
        data class Call(val path: String, val method: String)

        val calls = mutableListOf<Call>()
        val responses = ArrayDeque<JSONObject>()
        var failuresRemaining = 0

        override suspend fun request(
            endpoint: Endpoint,
            method: String,
            body: JSONObject?,
            requiresAuth: Boolean
        ): JSONObject {
            calls += Call(endpoint.path, method)
            if (failuresRemaining > 0) {
                failuresRemaining--
                throw IllegalStateException("Service unavailable")
            }
            return responses.removeFirst()
        }

        override suspend fun requestRaw(
            endpoint: Endpoint,
            method: String,
            body: JSONObject?,
            requiresAuth: Boolean
        ): String = request(endpoint, method, body, requiresAuth).toString()
    }

    private fun postsResponse(vararg posts: JSONObject): JSONObject = JSONObject()
        .put("posts", JSONArray(posts.toList()))
        .put("pagination", JSONObject().put("total", posts.size).put("page", 1).put("limit", 10).put("totalPages", 1))

    private fun postJson(id: String, title: String, content: String): JSONObject = JSONObject()
        .put("_id", id)
        .put("title", title)
        .put("content", content)
        .put("author", JSONObject().put("userId", "author-1").put("name", "Test author"))
        .put("createdAt", "2026-10-03T00:00:00Z")
        .put("readTime", 2)
        .put("views", 4)
        .put("likes", 1)
        .put("comments", JSONArray())
        .put("status", "published")
        .put("coverImage", JSONObject.NULL)
}
