package com.example.postly.ui.views.posts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.postly.ui.components.PostCardView
import com.example.postly.ui.theme.PostlyBlue
import com.example.postly.ui.theme.PostlyText
import com.example.postly.viewmodels.PostsViewModel
import kotlinx.coroutines.launch

@Composable
fun PostsView(viewModel: PostsViewModel, modifier: Modifier = Modifier) {
    LaunchedEffect(Unit) { viewModel.fetchPosts(reset = true) }
    LazyColumn(modifier.fillMaxSize().background(Color(0xFFF8F9FB)), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) { Text("Recent Posts", fontSize = 30.sp, fontWeight = FontWeight.Bold, color = PostlyText); Text("Explore the most interesting conversations", color = Color.Gray, textAlign = TextAlign.Center) } }
        when {
            viewModel.isLoading && viewModel.posts.isEmpty() -> item { LoadingState() }
            viewModel.errorMessage != null && viewModel.posts.isEmpty() -> item { ErrorState(viewModel.errorMessage!!) { viewModel.fetchPosts(reset = true) } }
            viewModel.posts.isEmpty() -> item { Text("No posts yet.", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color.Gray) }
            else -> {
                items(viewModel.posts, key = { it.id }) { PostCardView(it) }
                if (viewModel.canLoadMore) item { LaunchedEffect(Unit) { viewModel.loadMore() }; LoadingState() }
                if (viewModel.errorMessage != null) item { ErrorState(viewModel.errorMessage!!) { viewModel.fetchPosts() } }
            }
        }
    }
}

@Composable private fun LoadingState() = Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PostlyBlue) }
@Composable private fun ErrorState(message: String, retry: suspend () -> Unit) { val scope = rememberCoroutineScope(); Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center); TextButton(onClick = { scope.launch { retry() } }) { Text("Retry") } } }
