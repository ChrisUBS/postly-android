package com.example.postly.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.tasks.Tasks
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PushTokenSync {
    private const val PREFERENCES = "postly_push"
    private const val TOKEN_KEY = "fcmToken"

    fun hasPermission(context: Context): Boolean {
        val runtimePermissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        return runtimePermissionGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun isFirebaseConfigured(context: Context): Boolean =
        FirebaseApp.getApps(context).any { it.name == FirebaseApp.DEFAULT_APP_NAME }

    fun savedToken(context: Context): String? =
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).getString(TOKEN_KEY, null)

    fun saveToken(context: Context, token: String) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit().putString(TOKEN_KEY, token).apply()
    }

    suspend fun sync(context: Context, service: NotificationService) {
        if (!hasPermission(context) || !isFirebaseConfigured(context)) return
        val token = withContext(Dispatchers.IO) {
            Tasks.await(FirebaseMessaging.getInstance().token)
        }
        register(context, service, token)
    }

    suspend fun register(context: Context, service: NotificationService, token: String) {
        val previousToken = savedToken(context)
        if (!previousToken.isNullOrBlank() && previousToken != token) {
            runCatching { service.unregisterDeviceToken(previousToken) }
        }
        service.registerDeviceToken(token)
        saveToken(context, token)
    }

    suspend fun unregister(context: Context, service: NotificationService) {
        val token = savedToken(context)
        if (!token.isNullOrBlank()) runCatching { service.unregisterDeviceToken(token) }
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            .edit().remove(TOKEN_KEY).apply()
    }
}
