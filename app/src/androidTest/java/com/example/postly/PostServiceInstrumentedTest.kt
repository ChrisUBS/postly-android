package com.example.postly

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.postly.networking.ApiRequester
import com.example.postly.networking.Endpoint
import com.example.postly.services.PostService
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PostServiceInstrumentedTest {
    @Test
    fun readPost_requestsTheSelectedPost() = runBlocking {
        val requester = FakeApiRequester().apply { response = postJson("post-42", "Read me") }
        val service = PostService(requester)

        val post = service.getPostById("post-42")

        assertEquals("post-42", post.id)
        assertEquals("Read me", post.title)
        assertEquals("posts/post-42", requester.calls.single().path)
        assertEquals("GET", requester.calls.single().method)
        assertNull(requester.calls.single().body)
    }

    @Test
    fun createPost_sendsPostFieldsAndParsesCreatedPost() = runBlocking {
        val requester = FakeApiRequester().apply { response = postJson("created-1", "New post") }
        val service = PostService(requester)

        val created = service.createPost("New post", "Post body", "draft", null)

        val call = requester.calls.single()
        assertEquals("created-1", created.id)
        assertEquals("posts", call.path)
        assertEquals("POST", call.method)
        assertTrue(call.requiresAuth)
        assertEquals("New post", call.body?.getString("title"))
        assertEquals("Post body", call.body?.getString("content"))
        assertEquals("draft", call.body?.getString("status"))
        assertTrue(call.body?.isNull("coverImage") == true)
    }

    @Test
    fun updatePost_sendsChangesAndDeletePostRemovesSelectedPost() = runBlocking {
        val requester = FakeApiRequester().apply { response = postJson("post-7", "Updated title") }
        val service = PostService(requester)

        val updated = service.updatePost("post-7", "Updated title", "Updated body", "published", "https://img.test/cover.jpg")
        service.deletePost("post-7")

        assertEquals("Updated title", updated.title)
        assertEquals(2, requester.calls.size)

        val updateCall = requester.calls[0]
        assertEquals("posts/post-7", updateCall.path)
        assertEquals("PUT", updateCall.method)
        assertTrue(updateCall.requiresAuth)
        assertEquals("Updated body", updateCall.body?.getString("content"))
        assertEquals("https://img.test/cover.jpg", updateCall.body?.getString("coverImage"))

        val deleteCall = requester.calls[1]
        assertEquals("posts/post-7", deleteCall.path)
        assertEquals("DELETE", deleteCall.method)
        assertTrue(deleteCall.requiresAuth)
        assertNull(deleteCall.body)
    }

    @Test
    fun likeUnlikeAndCheckLike_useAuthenticatedLikeEndpoint() = runBlocking {
        val requester = FakeApiRequester()
        val service = PostService(requester)

        service.likePost("post-9")
        requester.response = JSONObject().put("liked", false)
        service.unlikePost("post-9")
        requester.response = JSONObject().put("liked", true)
        val liked = service.checkLike("post-9")

        assertTrue(liked)
        assertEquals(listOf("POST", "DELETE", "GET"), requester.calls.map { it.method })
        assertTrue(requester.calls.all { it.path == "posts/post-9/like" && it.requiresAuth })
        assertTrue(requester.calls.all { it.body == null })
    }

    private fun postJson(id: String, title: String) = JSONObject()
        .put("_id", id)
        .put("title", title)
        .put("content", "A post body")
        .put("author", JSONObject().put("userId", "author-1").put("name", "Test author"))
        .put("createdAt", "2026-10-03T00:00:00Z")
        .put("readTime", 2)
        .put("views", 4)
        .put("likes", 1)
        .put("comments", JSONArray())
        .put("status", "published")
        .put("coverImage", JSONObject.NULL)

    private data class RecordedCall(
        val path: String,
        val method: String,
        val body: JSONObject?,
        val requiresAuth: Boolean
    )

    private class FakeApiRequester : ApiRequester {
        val calls = mutableListOf<RecordedCall>()
        var response = JSONObject()

        override suspend fun request(endpoint: Endpoint, method: String, body: JSONObject?, requiresAuth: Boolean): JSONObject {
            calls += RecordedCall(endpoint.path, method, body, requiresAuth)
            return response
        }
    }
}
