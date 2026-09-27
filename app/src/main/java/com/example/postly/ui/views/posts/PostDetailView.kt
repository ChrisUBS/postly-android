package com.example.postly.ui.views.posts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.postly.managers.SessionManager
import com.example.postly.models.Author
import com.example.postly.models.Comment
import com.example.postly.models.Post
import com.example.postly.ui.theme.MutedText
import com.example.postly.ui.theme.PostlyBlue
import com.example.postly.ui.theme.PostlyText
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PostDetailView(postId: String, session: SessionManager, onBack: () -> Unit, modifier: Modifier = Modifier) {
    var post by remember(postId) { mutableStateOf<Post?>(null) }
    var loading by remember(postId) { mutableStateOf(true) }
    var error by remember(postId) { mutableStateOf<String?>(null) }
    var liked by remember(postId) { mutableStateOf(false) }
    var liking by remember { mutableStateOf(false) }
    var newComment by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var deletingId by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun load() {
        loading = true; error = null
        runCatching { session.postService.getPostById(postId) }.onSuccess {
            post = it
            liked = if (session.isAuthenticated) runCatching { session.postService.checkLike(it.id) }.getOrDefault(false) else false
        }.onFailure { error = it.message ?: "Unable to load this post." }
        loading = false
    }
    LaunchedEffect(postId) { load() }

    Column(modifier.fillMaxSize().background(Color(0xFFF8F9FB))) {
        Row(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Text("Post", fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = PostlyText)
        }
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PostlyBlue) }
            error != null -> ErrorContent(error!!) { scope.launch { load() } }
            post != null -> PostContent(post!!, session, liked, liking, newComment, sending, deletingId, { newComment = it }, { scope.launch {
                if (!session.isAuthenticated) { error = "Sign in to comment."; return@launch }
                sending = true
                runCatching { session.commentService.createComment(post!!.id, newComment.trim()) }.onSuccess { created -> post = post!!.copy(comments = post!!.comments + created); newComment = "" }.onFailure { error = it.message ?: "Unable to post your comment." }
                sending = false
            } }, { scope.launch {
                if (!session.isAuthenticated || liking) return@launch
                liking = true
                runCatching { if (liked) session.postService.unlikePost(post!!.id) else session.postService.likePost(post!!.id) }.onSuccess {
                    liked = !liked; post = post!!.copy(likes = (post!!.likes + if (liked) 1 else -1).coerceAtLeast(0))
                }.onFailure { error = it.message ?: "Unable to update like." }
                liking = false
            } }, { pendingDelete = it })
        }
    }
    pendingDelete?.let { commentId ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete comment?") },
            text = { Text("This action cannot be undone.") },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        deletingId = commentId
                        runCatching { session.commentService.deleteComment(postId, commentId) }
                            .onSuccess { post = post?.copy(comments = post!!.comments.filterNot { it.id == commentId }) }
                            .onFailure { error = it.message ?: "Unable to delete comment." }
                        deletingId = null
                        pendingDelete = null
                    }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            }
        )
    }
}

@Composable
private fun PostContent(post: Post, session: SessionManager, liked: Boolean, liking: Boolean, newComment: String, sending: Boolean, deletingId: String?, onCommentChange: (String) -> Unit, onSend: () -> Unit, onLike: () -> Unit, onDelete: (String) -> Unit) {
    Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 4.dp) {
            Column {
                post.coverImage?.takeIf { it.isNotBlank() }?.let { AsyncImage(it, "Cover image", Modifier.fillMaxWidth().heightIn(max = 300.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)), contentScale = ContentScale.Fit) }
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(post.title, fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold, color = PostlyText)
                    Row(verticalAlignment = Alignment.CenterVertically) { Avatar(post.author); Spacer(Modifier.width(10.dp)); Text(post.author.name, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                    MetaRow(Icons.Default.CalendarToday, detailDate(post.createdAt)); MetaRow(Icons.Default.Schedule, "${post.readTime} minutes of reading"); MetaRow(Icons.Default.Visibility, "${post.views} views")
                    Text(post.content, color = PostlyText, lineHeight = 24.sp)
                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically) { IconButton(onClick = onLike, enabled = session.isAuthenticated && !liking) { Icon(if (liked) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt, "Like", tint = if (liked) PostlyBlue else MutedText) }; Text("${post.likes} Like", color = if (liked) PostlyBlue else MutedText) }
                }
            }
        }
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 4.dp) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.ChatBubbleOutline, null, tint = PostlyBlue); Spacer(Modifier.width(8.dp)); Text("Comments (${post.comments.size})", fontSize = 22.sp, fontWeight = FontWeight.Bold) }
            if (session.isAuthenticated) { OutlinedTextField(newComment, onCommentChange, Modifier.fillMaxWidth().height(120.dp), placeholder = { Text("Write a comment...") }, shape = RoundedCornerShape(10.dp), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)); Button(onSend, Modifier.align(Alignment.End), enabled = newComment.trim().isNotEmpty() && !sending, colors = ButtonDefaults.buttonColors(containerColor = PostlyBlue, contentColor = Color.White)) { if (sending) CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp) else { Icon(Icons.Default.Send, null); Spacer(Modifier.width(8.dp)); Text("Comment") } } } else Text("Sign in to comment", Modifier.fillMaxWidth().background(Color(0xFFF2F2F7), RoundedCornerShape(10.dp)).padding(16.dp), textAlign = TextAlign.Center, color = MutedText)
            if (post.comments.isEmpty()) Text("There are no comments yet. Be the first to comment!", Modifier.fillMaxWidth().padding(vertical = 16.dp), color = MutedText, textAlign = TextAlign.Center) else post.comments.forEach { comment -> CommentRow(comment, canDelete = session.user?.userId == comment.author.userId || session.user?.userId == post.author.userId, deleting = deletingId == comment.id, onDelete = { onDelete(comment.id) }) }
        } }
    }
}

@Composable
private fun CommentRow(comment: Comment, canDelete: Boolean, deleting: Boolean, onDelete: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Avatar(comment.author, 40)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(comment.author.name, fontWeight = FontWeight.Bold)
            Text(detailDate(comment.createdAt), color = MutedText, fontSize = 12.sp)
            Spacer(Modifier.height(5.dp))
            Text(comment.content, color = PostlyText)
        }
        if (canDelete) IconButton(onClick = onDelete, enabled = !deleting) {
            if (deleting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else Icon(Icons.Default.DeleteOutline, "Delete", tint = MaterialTheme.colorScheme.error)
        }
    }
}
@Composable private fun Avatar(author: Author, size: Int = 42) { author.profilePicture?.takeIf { it.isNotBlank() }?.let { AsyncImage(it, "${author.name} profile image", Modifier.size(size.dp).clip(CircleShape), contentScale = ContentScale.Crop) } ?: Box(Modifier.size(size.dp).background(Color(0xFFE5E5EA), CircleShape), contentAlignment = Alignment.Center) { Text(author.name.split(" ").take(2).joinToString("") { it.take(1) }.uppercase(), color = MutedText, fontWeight = FontWeight.Bold) } }
@Composable private fun MetaRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = MutedText, modifier = Modifier.size(17.dp)); Spacer(Modifier.width(8.dp)); Text(text, color = MutedText, fontSize = 14.sp) } }
@Composable private fun ErrorContent(message: String, retry: () -> Unit) { Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text(message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center); TextButton(onClick = retry) { Text("Retry") } } }
private fun detailDate(value: String): String = runCatching { Instant.parse(value).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MMMM dd, yyyy · h:mm a")) }.getOrDefault(value.take(10))
