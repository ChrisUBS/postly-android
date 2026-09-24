package com.example.postly.networking

import com.example.postly.BuildConfig
import com.example.postly.managers.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class APIClient(private val authManager: AuthManager) {
    suspend fun request(endpoint: Endpoint, method: String = "GET", body: JSONObject? = null, requiresAuth: Boolean = false): JSONObject = withContext(Dispatchers.IO) {
        val baseUrl = BuildConfig.API_BASE_URL.trim()
        if (baseUrl.isBlank()) throw APIException.Configuration("API_BASE_URL is missing. Set it in secrets.properties.")
        val url = URL(baseUrl.trimEnd('/') + "/" + endpoint.path)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
            if (requiresAuth) authManager.accessToken?.let { setRequestProperty("Authorization", "Bearer $it") }
            if (body != null) {
                doOutput = true
                outputStream.bufferedWriter().use { it.write(body.toString()) }
            }
        }
        try {
            val status = connection.responseCode
            val response = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (status !in 200..299) throw APIException.Server(status, response.ifBlank { "Request failed." })
            if (response.isBlank()) JSONObject() else JSONObject(response)
        } finally {
            connection.disconnect()
        }
    }
}

sealed class APIException(message: String) : Exception(message) {
    class Configuration(message: String) : APIException(message)
    class Server(val statusCode: Int, message: String) : APIException(message)
}
