package com.example.postly.services

import com.example.postly.managers.AuthManager
import com.example.postly.models.AuthResponse
import com.example.postly.models.User
import com.example.postly.networking.APIClient
import com.example.postly.networking.Endpoint
import org.json.JSONObject

class AuthService(private val apiClient: APIClient, private val authManager: AuthManager) {
    suspend fun login(email: String, password: String): AuthResponse {
        val response = AuthResponse.fromJson(apiClient.request(Endpoint.LoginEmail, "POST", JSONObject().put("email", email).put("password", password)))
        authManager.accessToken = response.accessToken
        return response
    }

    suspend fun register(name: String, email: String, password: String): AuthResponse {
        val response = AuthResponse.fromJson(apiClient.request(Endpoint.RegisterEmail, "POST", JSONObject().put("name", name).put("email", email).put("password", password)))
        authManager.accessToken = response.accessToken
        return response
    }

    suspend fun checkAuth(): User = User.fromJson(apiClient.request(Endpoint.CheckAuth, requiresAuth = true))
}
