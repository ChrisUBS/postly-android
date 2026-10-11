package com.example.postly.managers

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.postly.models.User
import com.example.postly.networking.APIClient
import com.example.postly.services.AuthService
import com.example.postly.services.PushTokenSync

class SessionManager(context: Context) {
    private val applicationContext = context.applicationContext
    private val authManager = AuthManager(applicationContext)
    private val apiClient = APIClient(authManager)
    private val authService = AuthService(apiClient, authManager)
    val postService = com.example.postly.services.PostService(apiClient)
    val commentService = com.example.postly.services.CommentService(apiClient)
    val notificationService = com.example.postly.services.NotificationService(apiClient)

    var user: User? by mutableStateOf(null)
        private set
    var isLoading: Boolean by mutableStateOf(false)
        private set
    val isAuthenticated: Boolean get() = user != null

    suspend fun login(email: String, password: String): String? = runRequest { authService.login(email, password).user }
    suspend fun loginWithGoogle(idToken: String): String? = runRequest { authService.loginWithGoogle(idToken).user }
    suspend fun register(name: String, email: String, password: String): String? = runRequest { authService.register(name, email, password).user }

    suspend fun checkSession() {
        if (authManager.accessToken == null) return
        runCatching { authService.checkAuth() }.onSuccess { user = it }.onFailure { logout() }
    }

    suspend fun logout() {
        PushTokenSync.unregister(applicationContext, notificationService)
        authManager.logout()
        user = null
    }

    private suspend fun runRequest(request: suspend () -> User): String? {
        isLoading = true
        return try { user = request(); null } catch (error: Exception) { error.message ?: "Something went wrong." } finally { isLoading = false }
    }
}
