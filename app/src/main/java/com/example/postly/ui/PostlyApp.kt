package com.example.postly.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.postly.ui.components.NavbarView
import com.example.postly.ui.components.SideMenuView
import com.example.postly.ui.navigation.AppScreen
import com.example.postly.ui.theme.*
import com.example.postly.ui.views.WelcomeView
import com.example.postly.ui.views.auth.AuthScreenMode
import com.example.postly.ui.views.auth.AuthScreenView
import com.example.postly.managers.SessionManager
import com.example.postly.ui.views.posts.PostsView
import com.example.postly.viewmodels.PostsViewModel
import com.example.postly.ui.views.posts.PostDetailView
import com.example.postly.ui.views.user.ProfileView
import com.example.postly.ui.views.posts.CreatePostView
import com.example.postly.ui.views.posts.EditPostView

@Composable
fun PostlyApp() {
    var screen by remember { mutableStateOf(AppScreen.HOME) }; var menuOpen by remember { mutableStateOf(false) }; var searchExpanded by remember { mutableStateOf(false) }; var query by remember { mutableStateOf("") }
    var selectedPostId by remember { mutableStateOf<String?>(null) }
    var editingPostId by remember { mutableStateOf<String?>(null) }
    var postDetailBackScreen by remember { mutableStateOf(AppScreen.POSTS) }
    val context = LocalContext.current.applicationContext
    val session = remember(context) { SessionManager(context) }
    val postsViewModel = remember(session) { PostsViewModel(session.postService) }
    LaunchedEffect(session) { session.checkSession() }
    LaunchedEffect(session.isAuthenticated) {
        if (session.isAuthenticated && screen == AppScreen.HOME) screen = AppScreen.POSTS
    }
    if (screen == AppScreen.LOGIN || screen == AppScreen.REGISTER) {
        AuthScreenView(
            mode = if (screen == AppScreen.LOGIN) AuthScreenMode.LOGIN else AuthScreenMode.REGISTER,
            session = session,
            onBack = { screen = AppScreen.HOME },
            onSwitchMode = { screen = if (screen == AppScreen.LOGIN) AppScreen.REGISTER else AppScreen.LOGIN },
            onAuthenticated = { screen = AppScreen.POSTS }
        )
        return
    }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            NavbarView(searchExpanded, query, menuOpen, { query = it }, { searchExpanded = !searchExpanded }, { if (query.isNotBlank()) { screen = AppScreen.SEARCH; menuOpen = false } }, { menuOpen = !menuOpen })
            when (screen) {
                AppScreen.HOME -> WelcomeView(onJoin = { screen = AppScreen.REGISTER })
                AppScreen.POSTS -> PostsView(postsViewModel, onPostClick = { selectedPostId = it; postDetailBackScreen = AppScreen.POSTS; screen = AppScreen.POST_DETAIL })
                AppScreen.POST_DETAIL -> selectedPostId?.let { PostDetailView(it, session, onBack = { screen = postDetailBackScreen }) }
                AppScreen.SEARCH -> PlaceholderScreen("Search results", "Resultados para “$query”")
                AppScreen.CREATE_POST -> CreatePostView(session, onPostCreated = { screen = AppScreen.PROFILE }, onBack = { screen = AppScreen.PROFILE })
                AppScreen.EDIT_POST -> editingPostId?.let { EditPostView(it, session, onPostUpdated = { screen = AppScreen.PROFILE }, onBack = { screen = AppScreen.PROFILE }) }
                AppScreen.PROFILE -> ProfileView(
                    session = session,
                    onCreatePost = { screen = AppScreen.CREATE_POST },
                    onPostClick = { selectedPostId = it; postDetailBackScreen = AppScreen.PROFILE; screen = AppScreen.POST_DETAIL },
                    onEditPost = { editingPostId = it; screen = AppScreen.EDIT_POST }
                )
                else -> Unit
            }
        }
        AnimatedVisibility(menuOpen, enter = fadeIn(tween(180)), exit = fadeOut(tween(180))) { Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .22f)).clickable { menuOpen = false }) }
        AnimatedVisibility(menuOpen, enter = slideInHorizontally(tween(260)) { -it }, exit = slideOutHorizontally(tween(220)) { -it }) {
            SideMenuView(
                isAuthenticated = session.isAuthenticated,
                onNavigate = { screen = it; menuOpen = false },
                onLogin = { menuOpen = false; screen = AppScreen.LOGIN },
                onLogout = { session.logout(); menuOpen = false; screen = AppScreen.HOME }
            )
        }
    }
}
@Composable private fun PlaceholderScreen(title: String, subtitle: String) { Column(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(WelcomeTop, WelcomeBottom))).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PostlyText); Spacer(Modifier.height(8.dp)); Text(subtitle, color = MutedText) } }
