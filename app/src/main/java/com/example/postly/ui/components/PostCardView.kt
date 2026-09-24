package com.example.postly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.postly.models.Post
import com.example.postly.ui.theme.MutedText
import com.example.postly.ui.theme.PostlyText
import coil.compose.AsyncImage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PostCardView(post: Post, modifier: Modifier = Modifier) {
    Card(modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(12.dp)), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column {
            post.coverImage?.takeIf { it.isNotBlank() }?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Cover image for ${post.title}",
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(post.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PostlyText)
                Text(post.content, color = MutedText, maxLines = if (post.coverImage == null) 6 else 3, overflow = TextOverflow.Ellipsis, lineHeight = 20.sp)
            }
            HorizontalDivider()
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    PostMeta(Icons.Default.CalendarToday, formatDate(post.createdAt))
                    PostMeta(Icons.Default.Schedule, "${post.readTime} min")
                }
                Spacer(Modifier.weight(1f))
                PostMeta(Icons.Default.ThumbUpOffAlt, post.likes.toString())
                Spacer(Modifier.width(14.dp))
                PostMeta(Icons.Default.ChatBubbleOutline, post.commentsCount.toString())
            }
        }
    }
}

@Composable private fun PostMeta(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = MutedText, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(5.dp)); Text(value, color = MutedText, fontSize = 12.sp) } }

private fun formatDate(value: String): String = runCatching { Instant.parse(value).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) }.getOrDefault(value.take(10))
