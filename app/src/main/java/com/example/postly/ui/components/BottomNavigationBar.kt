package com.example.postly.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.postly.ui.navigation.AppScreen
import com.example.postly.ui.theme.PostlyBlue
import com.example.postly.ui.theme.MutedText

@Composable
fun BottomNavigationBar(isAuthenticated: Boolean, currentScreen: AppScreen, onNavigate: (AppScreen) -> Unit, modifier: Modifier = Modifier) {
    val items = if (isAuthenticated) listOf(
        NavigationItem(AppScreen.POSTS, "Posts", Icons.AutoMirrored.Filled.Chat),
        NavigationItem(AppScreen.CREATE_POST, "Create", Icons.Default.Create),
        NavigationItem(AppScreen.PROFILE, "Profile", Icons.Default.Person)
    ) else listOf(
        NavigationItem(AppScreen.POSTS, "Posts", Icons.AutoMirrored.Filled.Chat),
        NavigationItem(AppScreen.LOGIN, "Log In", Icons.Default.Person)
    )
    val selected = when (currentScreen) {
        AppScreen.POST_DETAIL -> AppScreen.POSTS
        AppScreen.EDIT_POST -> AppScreen.CREATE_POST
        AppScreen.REGISTER -> AppScreen.LOGIN
        else -> currentScreen
    }
    NavigationBar(modifier.fillMaxWidth(), containerColor = Color.White, contentColor = MutedText, tonalElevation = 8.dp) {
        items.forEach { item ->
            NavigationBarItem(
                selected = selected == item.screen,
                onClick = { onNavigate(item.screen) },
                icon = { Icon(item.icon, item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PostlyBlue,
                    selectedTextColor = PostlyBlue,
                    unselectedIconColor = MutedText,
                    unselectedTextColor = MutedText,
                    indicatorColor = PostlyBlue.copy(alpha = .12f)
                )
            )
        }
    }
}

private data class NavigationItem(val screen: AppScreen, val label: String, val icon: ImageVector)
