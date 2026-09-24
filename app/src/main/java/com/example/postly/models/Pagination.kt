package com.example.postly.models

import org.json.JSONObject

data class Pagination(val total: Int, val page: Int, val limit: Int, val totalPages: Int) {
    companion object {
        fun fromJson(json: JSONObject) = Pagination(json.optInt("total"), json.optInt("page", 1), json.optInt("limit", 10), json.optInt("totalPages", 1))
    }
}
