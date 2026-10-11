package com.example.postly.ui.views.notifications

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.postly.models.AppNotification
import com.example.postly.ui.theme.MutedText
import com.example.postly.ui.theme.PostlyBlue
import com.example.postly.ui.theme.PostlyText
import com.example.postly.services.NotificationService
import com.example.postly.services.PushTokenSync
import com.example.postly.viewmodels.NotificationsViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun NotificationsView(
    viewModel: NotificationsViewModel,
    notificationService: NotificationService,
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var notificationsAllowed by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(PushTokenSync.hasPermission(context))
    }
    var permissionPrompted by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(context.getSharedPreferences("postly_push", 0).getBoolean("permissionPrompted", false))
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationsAllowed = PushTokenSync.hasPermission(context)
        if (granted) scope.launch { runCatching { PushTokenSync.sync(context, notificationService) } }
    }

    LaunchedEffect(Unit) { viewModel.refresh() }

    LazyColumn(
        modifier.fillMaxSize().background(Color(0xFFF8F9FB)),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Notifications", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PostlyText)
                    Text(if (viewModel.unreadCount == 1) "1 unread" else "${viewModel.unreadCount} unread", color = MutedText)
                }
                TextButton(onClick = { scope.launch { viewModel.markAllRead() } }, enabled = viewModel.unreadCount > 0) {
                    Text("Mark all as read")
                }
            }
        }

        if (!notificationsAllowed) {
            item {
                Column(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(14.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Get alerts when someone likes or comments on your posts.", color = PostlyText)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissionPrompted) {
                        Button(onClick = {
                            permissionPrompted = true
                            context.getSharedPreferences("postly_push", 0).edit().putBoolean("permissionPrompted", true).apply()
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }) { Text("Enable push notifications") }
                    } else {
                        OutlinedButton(onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                            )
                        }) { Text("Open notification settings") }
                    }
                }
            }
        }

        if (viewModel.errorMessage != null) {
            item {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(viewModel.errorMessage!!, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { scope.launch { viewModel.refresh() } }) { Text("Retry") }
                }
            }
        }

        if (viewModel.isLoading && viewModel.notifications.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PostlyBlue)
                }
            }
        } else if (viewModel.notifications.isEmpty()) {
            item { EmptyNotifications() }
        } else {
            items(viewModel.notifications, key = { it.id }) { notification ->
                NotificationRow(
                    notification = notification,
                    onOpen = {
                        scope.launch { viewModel.markRead(notification) }
                        notification.postId.takeIf { it.isNotBlank() }?.let(onPostClick)
                    },
                    onMarkRead = { scope.launch { viewModel.markRead(notification) } }
                )
            }
        }
    }
}

@Composable
private fun NotificationRow(notification: AppNotification, onOpen: () -> Unit, onMarkRead: () -> Unit) {
    val isLike = notification.type.equals("like", ignoreCase = true)
    Card(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (notification.isRead) Color.White else Color(0xFFEAF3FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(start = 14.dp, end = 4.dp, top = 14.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).background(PostlyBlue.copy(alpha = .12f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isLike) Icons.Default.ThumbUpOffAlt else Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = PostlyBlue
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "${notification.actorName} ${if (isLike) "liked" else "commented on"} your post",
                    fontWeight = FontWeight.SemiBold,
                    color = PostlyText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(notification.postTitle, color = MutedText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(formatDate(notification.createdAt), color = MutedText, fontSize = 12.sp)
            }
            if (!notification.isRead) {
                IconButton(onClick = onMarkRead, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Done, "Mark as read", tint = PostlyBlue)
                }
            }
        }
    }
}

@Composable
private fun EmptyNotifications() {
    Column(Modifier.fillMaxWidth().padding(top = 48.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(Icons.Default.NotificationsNone, null, modifier = Modifier.size(42.dp), tint = MutedText)
        Text("You're all caught up", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = PostlyText)
        Text("Likes and comments on your posts will show up here.", color = MutedText)
    }
}

private fun formatDate(value: String): String = runCatching {
    Instant.parse(value).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a"))
}.getOrDefault(value.take(16).replace('T', ' '))
