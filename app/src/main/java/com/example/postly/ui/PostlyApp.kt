package com.example.postly.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.material3.Text
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

@Composable
fun PostlyApp() {
    var screen by remember { mutableStateOf(AppScreen.HOME) }; var menuOpen by remember { mutableStateOf(false) }; var searchExpanded by remember { mutableStateOf(false) }; var query by remember { mutableStateOf("") }
    if (screen == AppScreen.LOGIN || screen == AppScreen.REGISTER) {
        AuthScreenView(
            mode = if (screen == AppScreen.LOGIN) AuthScreenMode.LOGIN else AuthScreenMode.REGISTER,
            onBack = { screen = AppScreen.HOME },
            onSwitchMode = { screen = if (screen == AppScreen.LOGIN) AppScreen.REGISTER else AppScreen.LOGIN }
        )
        return
    }
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            NavbarView(searchExpanded, query, menuOpen, { query = it }, { searchExpanded = !searchExpanded }, { if (query.isNotBlank()) { screen = AppScreen.SEARCH; menuOpen = false } }, { menuOpen = !menuOpen })
            when (screen) { AppScreen.HOME -> WelcomeView(onJoin = { screen = AppScreen.REGISTER }); AppScreen.POSTS -> PlaceholderScreen("Posts", "Here will appear all the posts."); AppScreen.SEARCH -> PlaceholderScreen("Search results", "Resultados para “$query”"); else -> Unit }
        }
        AnimatedVisibility(menuOpen, enter = fadeIn(tween(180)), exit = fadeOut(tween(180))) { Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .22f)).clickable { menuOpen = false }) }
        AnimatedVisibility(menuOpen, enter = slideInHorizontally(tween(260)) { -it }, exit = slideOutHorizontally(tween(220)) { -it }) { SideMenuView({ screen = it; menuOpen = false }, { menuOpen = false; screen = AppScreen.LOGIN }) }
    }
}
@Composable private fun PlaceholderScreen(title: String, subtitle: String) { Column(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(WelcomeTop, WelcomeBottom))).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PostlyText); Spacer(Modifier.height(8.dp)); Text(subtitle, color = MutedText) } }
