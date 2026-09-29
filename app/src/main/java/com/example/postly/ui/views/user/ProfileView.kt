package com.example.postly.ui.views.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.postly.managers.SessionManager
import com.example.postly.models.Post
import com.example.postly.ui.theme.MutedText
import com.example.postly.ui.theme.PostlyBlue
import com.example.postly.ui.theme.PostlyText
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ProfileView(session: SessionManager, onCreatePost: () -> Unit, onPostClick: (String) -> Unit, onEditPost: (String) -> Unit, onLogout: () -> Unit, modifier: Modifier = Modifier) {
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<Post?>(null) }
    var deletingId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun loadPosts() {
        loading = true; error = null
        runCatching { session.postService.getMyPosts().posts }
            .onSuccess { posts = it }
            .onFailure { error = it.message ?: "Failed to load your publications." }
        loading = false
    }
    LaunchedEffect(Unit) { loadPosts() }

    Column(modifier.fillMaxSize().background(Color(0xFFF8F9FB)).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        ProfileCard(session, onCreatePost, onLogout)
        PublicationsSection(posts, loading, error, deletingId, onCreatePost, onPostClick, onEditPost, onRetry = { scope.launch { loadPosts() } }, onDelete = { pendingDelete = it })
    }
    pendingDelete?.let { post ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete this post?") },
            text = { Text("This action cannot be undone.") },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        deletingId = post.id
                        runCatching { session.postService.deletePost(post.id) }
                            .onSuccess { posts = posts.filterNot { it.id == post.id } }
                            .onFailure { error = it.message ?: "Unable to delete this post." }
                        deletingId = null
                        pendingDelete = null
                    }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            }
        )
    }
}

@Composable
private fun ProfileCard(session: SessionManager, onCreatePost: () -> Unit, onLogout: () -> Unit) {
    val user = session.user
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 6.dp) {
        Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileAvatar(user?.profilePicture, user?.name ?: "User")
            Text(user?.name ?: "User", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PostlyText)
            user?.email?.takeIf { it.isNotBlank() }?.let { Text(it, color = MutedText) }
            Button(onClick = onCreatePost, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = PostlyBlue, contentColor = Color.White)) {
                Icon(Icons.Default.Create, null); Spacer(Modifier.width(8.dp)); Text("New post")
            }
            OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF3B30))) {
                Icon(Icons.AutoMirrored.Filled.Logout, null); Spacer(Modifier.width(8.dp)); Text("Log Out")
            }
        }
    }
}

@Composable
private fun PublicationsSection(posts: List<Post>, loading: Boolean, error: String?, deletingId: String?, onCreatePost: () -> Unit, onPostClick: (String) -> Unit, onEditPost: (String) -> Unit, onRetry: () -> Unit, onDelete: (Post) -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 4.dp) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Description, null, tint = PostlyBlue); Spacer(Modifier.width(10.dp)); Text("My publications", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = PostlyText) }
            when {
                loading -> Box(Modifier.fillMaxWidth().padding(36.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PostlyBlue) }
                error != null -> Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) { Text(error, color = MaterialTheme.colorScheme.error); TextButton(onClick = onRetry) { Text("Retry") } }
                posts.isEmpty() -> EmptyPublications(onCreatePost)
                else -> posts.forEachIndexed { index, post -> PublicationRow(post, deletingId == post.id, onPostClick, onEditPost, onDelete); if (index < posts.lastIndex) HorizontalDivider() }
            }
        }
    }
}

@Composable
private fun EmptyPublications(onCreatePost: () -> Unit) {
    Column(Modifier.fillMaxWidth().background(Color(0x0D000000), RoundedCornerShape(12.dp)).padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("You haven't created any posts yet.", color = MutedText)
        Button(onClick = onCreatePost, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = PostlyBlue, contentColor = Color.White)) { Icon(Icons.Default.Create, null); Spacer(Modifier.width(7.dp)); Text("Create new post") }
    }
}

@Composable
private fun PublicationRow(post: Post, deleting: Boolean, onPostClick: (String) -> Unit, onEditPost: (String) -> Unit, onDelete: (Post) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(post.title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = PostlyText, maxLines = 2, overflow = TextOverflow.Ellipsis)
            IconButton(onClick = { onPostClick(post.id) }) { Icon(Icons.Default.Visibility, "View", tint = MutedText) }
            IconButton(onClick = { onEditPost(post.id) }) { Icon(Icons.Default.Edit, "Edit", tint = MutedText) }
            IconButton(onClick = { onDelete(post) }, enabled = !deleting) { if (deleting) CircularProgressIndicator(Modifier.size(19.dp), strokeWidth = 2.dp) else Icon(Icons.Default.DeleteOutline, "Delete", tint = Color(0xFFFF3B30)) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(shortDate(post.createdAt), color = MutedText, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Circle, null, tint = if (post.status == "published") Color(0xFF34C759) else Color(0xFFFF9500), modifier = Modifier.size(9.dp)); Spacer(Modifier.width(4.dp)); Text(if (post.status == "published") "Published" else "Draft", color = MutedText, fontSize = 12.sp) }
            Text("${post.commentsCount} comments", color = MutedText, fontSize = 12.sp)
            Text("${post.views} views", color = MutedText, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ProfileAvatar(url: String?, name: String) {
    url?.takeIf { it.isNotBlank() }?.let { AsyncImage(it, "$name profile picture", Modifier.size(120.dp).clip(CircleShape), contentScale = ContentScale.Crop) }
        ?: Box(Modifier.size(120.dp).background(Color(0x4D8E8E93), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(48.dp)) }
}

private fun shortDate(value: String): String = runCatching { Instant.parse(value).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) }.getOrDefault(value.take(10))
