package com.example.postly.services

import com.example.postly.BuildConfig
import com.example.postly.models.PexelsPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

class PexelsService {
    suspend fun searchImages(query: String): List<PexelsPhoto> = withContext(Dispatchers.IO) {
        val key = BuildConfig.PEXELS_API_KEY.trim()
        if (key.isBlank() || query.isBlank()) return@withContext emptyList()
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val connection = (URL("https://api.pexels.com/v1/search?query=$encodedQuery&per_page=6").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"; connectTimeout = 15_000; readTimeout = 15_000; setRequestProperty("Authorization", key)
        }
        try {
            if (connection.responseCode !in 200..299) return@withContext emptyList()
            val response = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            val photos = response.optJSONArray("photos") ?: return@withContext emptyList()
            List(photos.length()) { PexelsPhoto.fromJson(photos.getJSONObject(it)) }
        } finally { connection.disconnect() }
    }
}
