package com.example.postly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.postly.ui.navigation.AppScreen
import com.example.postly.ui.theme.MutedText
import com.example.postly.ui.theme.PostlyBlue
import com.example.postly.ui.theme.PostlyText

@Composable
fun SideMenuView(isAuthenticated: Boolean, onNavigate: (AppScreen) -> Unit, onLogin: () -> Unit, onLogout: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.width(280.dp).fillMaxHeight().background(Color.White).padding(horizontal = 20.dp, vertical = 40.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.AutoMirrored.Filled.Chat, null, tint = PostlyBlue, modifier = Modifier.size(28.dp)); Spacer(Modifier.width(8.dp)); Text("Postly", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PostlyText) }
        Spacer(Modifier.height(24.dp)); HorizontalDivider(); Spacer(Modifier.height(20.dp))
        if (!isAuthenticated) MenuItem(Icons.Default.Home, "Home") { onNavigate(AppScreen.HOME) }
        MenuItem(Icons.AutoMirrored.Filled.Chat, "Posts") { onNavigate(AppScreen.POSTS) }
        if (isAuthenticated) {
            MenuItem(Icons.Default.Create, "Create Post") { onNavigate(AppScreen.CREATE_POST) }
            MenuItem(Icons.Default.Person, "My Profile") { onNavigate(AppScreen.PROFILE) }
        }
        Spacer(Modifier.weight(1f))
        if (isAuthenticated) {
            Button(onLogout, Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30), contentColor = Color.White)) { Icon(Icons.AutoMirrored.Filled.Logout, null); Spacer(Modifier.width(8.dp)); Text("Log Out", fontSize = 17.sp) }
        } else {
            Button(onLogin, Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = PostlyBlue, contentColor = Color.White)) { Text("Log In", fontSize = 17.sp) }
        }
    }
}
@Composable private fun MenuItem(icon: ImageVector, label: String, onClick: () -> Unit) { Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick).padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = MutedText, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(16.dp)); Text(label, fontSize = 20.sp, color = PostlyText) } }
