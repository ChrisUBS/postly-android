package com.example.postly.models

import org.json.JSONObject

data class PexelsPhoto(val id: Long, val photographer: String, val mediumUrl: String, val largeUrl: String, val alt: String?) {
    companion object {
        fun fromJson(json: JSONObject): PexelsPhoto {
            val sources = json.getJSONObject("src")
            return PexelsPhoto(json.getLong("id"), json.optString("photographer", "Pexels"), sources.optString("medium"), sources.optString("large"), json.optString("alt").takeIf { it.isNotBlank() })
        }
    }
}
