package com.example.postly.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import com.example.postly.managers.SessionManager
import com.example.postly.ui.components.BottomNavigationBar
import com.example.postly.ui.components.NavbarView
import com.example.postly.ui.navigation.AppScreen
import com.example.postly.ui.theme.*
import com.example.postly.ui.views.auth.AuthScreenMode
import com.example.postly.ui.views.auth.AuthScreenView
import com.example.postly.ui.views.posts.CreatePostView
import com.example.postly.ui.views.posts.EditPostView
import com.example.postly.ui.views.posts.PostDetailView
import com.example.postly.ui.views.posts.PostsView
import com.example.postly.ui.views.posts.SearchResultsView
import com.example.postly.ui.views.user.ProfileView
import com.example.postly.viewmodels.PostsViewModel

@Composable
fun PostlyApp() {
    var screen by remember { mutableStateOf(AppScreen.POSTS) }
    var searchExpanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("" ) }
    var searchQuery by remember { mutableStateOf("" ) }
    var navigationVisible by remember { mutableStateOf(true) }
    var selectedPostId by remember { mutableStateOf<String?>(null) }
    var editingPostId by remember { mutableStateOf<String?>(null) }
    var postDetailBackScreen by remember { mutableStateOf(AppScreen.POSTS) }
    val context = LocalContext.current.applicationContext
    val session = remember(context) { SessionManager(context) }
    val postsViewModel = remember(session) { PostsViewModel(session.postService) }

    LaunchedEffect(session) { session.checkSession() }
    LaunchedEffect(screen) { navigationVisible = true }

    val navigationScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput) {
                    when {
                        available.y < -3f -> navigationVisible = false
                        available.y > 3f -> navigationVisible = true
                    }
                }
                return Offset.Zero
            }
        }
    }

    fun closeSearch() {
        searchExpanded = false
        searchText = ""
        searchQuery = ""
        if (screen == AppScreen.SEARCH) screen = AppScreen.POSTS
    }

    BackHandler(enabled = searchExpanded) { closeSearch() }

    val handlesBack = !searchExpanded && when (screen) {
        AppScreen.LOGIN, AppScreen.REGISTER,
        AppScreen.POST_DETAIL,
        AppScreen.SEARCH,
        AppScreen.CREATE_POST,
        AppScreen.EDIT_POST,
        AppScreen.PROFILE -> true
        AppScreen.POSTS -> false
    }

    BackHandler(enabled = handlesBack) {
        when (screen) {
            AppScreen.LOGIN, AppScreen.REGISTER -> screen = AppScreen.POSTS
            AppScreen.POST_DETAIL -> screen = postDetailBackScreen
            AppScreen.SEARCH -> closeSearch()
            AppScreen.CREATE_POST, AppScreen.EDIT_POST -> screen = AppScreen.PROFILE
            AppScreen.PROFILE -> screen = AppScreen.POSTS
            AppScreen.POSTS -> Unit
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().nestedScroll(navigationScrollConnection),
        topBar = {
            AnimatedVisibility(
                visible = navigationVisible,
                enter = slideInVertically(tween(180)) { -it } + fadeIn(tween(150)),
                exit = slideOutVertically(tween(180)) { -it } + fadeOut(tween(120))
            ) {
                NavbarView(
                    isSearchExpanded = searchExpanded,
                    query = searchText,
                    onQueryChange = { searchText = it },
                    onSearchOpen = { searchExpanded = true },
                    onSearchClose = { closeSearch() },
                    onSearchSubmit = {
                        val trimmed = searchText.trim()
                        if (trimmed.isNotEmpty()) {
                            searchQuery = trimmed
                            screen = AppScreen.SEARCH
                        }
                    }
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = navigationVisible,
                enter = slideInVertically(tween(180)) { it } + fadeIn(tween(150)),
                exit = slideOutVertically(tween(180)) { it } + fadeOut(tween(120))
            ) {
                BottomNavigationBar(
                    isAuthenticated = session.isAuthenticated,
                    currentScreen = screen,
                    onNavigate = { destination -> navigationVisible = true; screen = destination }
                )
            }
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when (screen) {
                AppScreen.LOGIN, AppScreen.REGISTER -> AuthScreenView(
                    mode = if (screen == AppScreen.LOGIN) AuthScreenMode.LOGIN else AuthScreenMode.REGISTER,
                    session = session,
                    onSwitchMode = { screen = if (screen == AppScreen.LOGIN) AppScreen.REGISTER else AppScreen.LOGIN },
                    onAuthenticated = { screen = AppScreen.POSTS }
                )
                AppScreen.POSTS -> PostsView(postsViewModel, onPostClick = { selectedPostId = it; postDetailBackScreen = AppScreen.POSTS; screen = AppScreen.POST_DETAIL })
                AppScreen.POST_DETAIL -> selectedPostId?.let { PostDetailView(it, session, onBack = { screen = postDetailBackScreen }) }
                AppScreen.SEARCH -> SearchResultsView(
                    query = searchQuery,
                    session = session,
                    onPostClick = { selectedPostId = it; postDetailBackScreen = AppScreen.SEARCH; screen = AppScreen.POST_DETAIL },
                    onBackToPosts = { closeSearch() }
                )
                AppScreen.CREATE_POST -> CreatePostView(session, onPostCreated = { screen = AppScreen.PROFILE }, onBack = { screen = AppScreen.PROFILE })
                AppScreen.EDIT_POST -> editingPostId?.let { EditPostView(it, session, onPostUpdated = { screen = AppScreen.PROFILE }, onBack = { screen = AppScreen.PROFILE }) }
                AppScreen.PROFILE -> ProfileView(
                    session = session,
                    onCreatePost = { screen = AppScreen.CREATE_POST },
                    onPostClick = { selectedPostId = it; postDetailBackScreen = AppScreen.PROFILE; screen = AppScreen.POST_DETAIL },
                    onEditPost = { editingPostId = it; screen = AppScreen.EDIT_POST },
                    onLogout = { session.logout(); screen = AppScreen.POSTS }
                )
            }
        }
    }
}
