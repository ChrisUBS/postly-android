package com.example.postly.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.postly.models.AppNotification
import com.example.postly.services.NotificationService

class NotificationsViewModel(private val notificationService: NotificationService) {
    var notifications: List<AppNotification> by mutableStateOf(emptyList())
        private set
    var unreadCount: Int by mutableStateOf(0)
        private set
    var isLoading: Boolean by mutableStateOf(false)
        private set
    var errorMessage: String? by mutableStateOf(null)
        private set

    suspend fun refresh() {
        if (isLoading) return
        isLoading = true
        errorMessage = null
        runCatching { notificationService.getNotifications() }
            .onSuccess {
                notifications = it.notifications
                unreadCount = it.unreadCount
            }
            .onFailure { errorMessage = it.message ?: "Unable to load notifications." }
        isLoading = false
    }

    suspend fun markRead(notification: AppNotification) {
        if (notification.isRead) return
        runCatching { notificationService.markRead(notification.id) }
            .onSuccess {
                notifications = notifications.map { if (it.id == notification.id) it.copy(isRead = true) else it }
                unreadCount = (unreadCount - 1).coerceAtLeast(0)
            }
            .onFailure { errorMessage = it.message ?: "Unable to mark notification as read." }
    }

    suspend fun markAllRead() {
        if (unreadCount == 0) return
        runCatching { notificationService.markAllRead() }
            .onSuccess {
                notifications = notifications.map { it.copy(isRead = true) }
                unreadCount = 0
                errorMessage = null
            }
            .onFailure { errorMessage = it.message ?: "Unable to mark notifications as read." }
    }

    fun clear() {
        notifications = emptyList()
        unreadCount = 0
        errorMessage = null
    }
}
