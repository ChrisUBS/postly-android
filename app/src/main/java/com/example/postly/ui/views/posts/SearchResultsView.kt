package com.example.postly.ui.views.posts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.postly.managers.SessionManager
import com.example.postly.models.Post
import com.example.postly.ui.components.PostCardView
import com.example.postly.ui.theme.MutedText
import com.example.postly.ui.theme.PostlyBlue
import com.example.postly.ui.theme.PostlyText
import kotlinx.coroutines.launch

@Composable
fun SearchResultsView(
    query: String,
    session: SessionManager,
    onPostClick: (String) -> Unit,
    onBackToPosts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trimmedQuery = query.trim()
    var posts by remember(trimmedQuery) { mutableStateOf(emptyList<Post>()) }
    var isLoading by remember(trimmedQuery) { mutableStateOf(false) }
    var errorMessage by remember(trimmedQuery) { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun loadResults() {
        if (trimmedQuery.isEmpty()) {
            posts = emptyList()
            errorMessage = null
            isLoading = false
            return
        }

        isLoading = true
        errorMessage = null
        runCatching { session.postService.searchPosts(trimmedQuery) }
            .onSuccess { posts = it.posts }
            .onFailure { errorMessage = it.message ?: "Error searching. Try again." }
        isLoading = false
    }

    LaunchedEffect(trimmedQuery) {
        loadResults()
    }

    LazyColumn(
        modifier.fillMaxSize().background(Color(0xFFF8F9FB)),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = "Search results: \"$trimmedQuery\"",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = PostlyText
                )
                if (!isLoading && posts.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("${posts.size} result(s) were found.", color = MutedText)
                }
            }
        }

        when {
            isLoading -> item {
                Box(
                    Modifier.fillMaxWidth().padding(top = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PostlyBlue)
                }
            }
            errorMessage != null -> item {
                ErrorState(errorMessage!!, onRetry = { scope.launch { loadResults() } })
            }
            posts.isEmpty() -> item {
                EmptyState(onBackToPosts)
            }
            else -> items(posts, key = { it.id }) { post ->
                PostCardView(post, onClick = { onPostClick(post.id) })
            }
        }
    }
}

@Composable
private fun EmptyState(onBackToPosts: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "No results were found",
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = MutedText,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Try different search terms",
            fontSize = 18.sp,
            color = MutedText,
            textAlign = TextAlign.Center
        )
        Button(onClick = onBackToPosts) {
            Text("See all publications")
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        TextButton(onClick = onRetry) {
            Text("Retry")
        }
    }
}
