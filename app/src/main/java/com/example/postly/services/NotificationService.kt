package com.example.postly.services

import com.example.postly.models.AppNotification
import com.example.postly.networking.APIClient
import com.example.postly.networking.Endpoint
import org.json.JSONObject

data class NotificationPage(val notifications: List<AppNotification>, val unreadCount: Int)

class NotificationService(private val apiClient: APIClient) {
    suspend fun getNotifications(): NotificationPage {
        val response = apiClient.request(Endpoint.GetNotifications, requiresAuth = true)
        val notifications = response.optJSONArray("notifications")?.let { array ->
            List(array.length()) { AppNotification.fromJson(array.getJSONObject(it)) }
        }.orEmpty()
        val unreadCount = if (response.has("unreadCount")) {
            response.optInt("unreadCount")
        } else {
            notifications.count { !it.isRead }
        }
        return NotificationPage(notifications, unreadCount)
    }

    suspend fun markRead(id: String) {
        apiClient.request(Endpoint.MarkNotificationRead(id), method = "PATCH", requiresAuth = true)
    }

    suspend fun markAllRead() {
        apiClient.request(Endpoint.MarkAllNotificationsRead, method = "PATCH", requiresAuth = true)
    }

    suspend fun registerDeviceToken(token: String) {
        apiClient.request(
            Endpoint.RegisterNotificationDevice,
            method = "POST",
            body = JSONObject().put("token", token),
            requiresAuth = true
        )
    }

    suspend fun unregisterDeviceToken(token: String) {
        apiClient.request(
            Endpoint.UnregisterNotificationDevice,
            method = "DELETE",
            body = JSONObject().put("token", token),
            requiresAuth = true
        )
    }
}
