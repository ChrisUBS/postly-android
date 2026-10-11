package com.example.postly.services

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.postly.MainActivity
import com.example.postly.R
import com.example.postly.managers.AuthManager
import com.example.postly.managers.SessionManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PostlyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        val previousToken = PushTokenSync.savedToken(applicationContext)
        PushTokenSync.saveToken(applicationContext, token)
        if (!PushTokenSync.hasPermission(applicationContext) || AuthManager(applicationContext).accessToken == null) return

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val service = SessionManager(applicationContext).notificationService
            if (!previousToken.isNullOrBlank() && previousToken != token) {
                runCatching { service.unregisterDeviceToken(previousToken) }
            }
            runCatching { service.registerDeviceToken(token) }
                .onFailure { Log.w(TAG, "Unable to register refreshed FCM token", it) }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: getString(R.string.app_name)
        val body = message.notification?.body ?: return
        if (!PushTokenSync.hasPermission(applicationContext)) return

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_NOTIFICATION_POST_ID, message.data["postId"])
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            message.data["notificationId"]?.hashCode() ?: System.currentTimeMillis().toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        runCatching {
            NotificationManagerCompat.from(this).notify(
                message.data["notificationId"]?.hashCode() ?: System.currentTimeMillis().toInt(),
                notification
            )
        }.onFailure { Log.w(TAG, "Unable to display push notification", it) }
    }

    companion object {
        const val CHANNEL_ID = "postly_activity"
        private const val TAG = "PostlyPush"
    }
}
