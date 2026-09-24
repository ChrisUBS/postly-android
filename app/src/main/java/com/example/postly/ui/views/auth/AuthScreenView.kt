package com.example.postly.ui.views.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.postly.ui.theme.PostlyBlue
import com.example.postly.ui.theme.PostlyText
import com.example.postly.managers.SessionManager

enum class AuthScreenMode { LOGIN, REGISTER }

@Composable
fun AuthScreenView(
    mode: AuthScreenMode,
    session: SessionManager,
    onBack: () -> Unit,
    onSwitchMode: () -> Unit,
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(session.user) { if (session.isAuthenticated) onAuthenticated() }
    Column(modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState()).padding(top = 34.dp)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Spacer(Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.Chat, null, tint = PostlyBlue, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(8.dp))
            Text("Postly", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = PostlyText)
            Spacer(Modifier.weight(1f)); Spacer(Modifier.size(48.dp))
        }
        Text(if (mode == AuthScreenMode.LOGIN) "Log In" else "Sign Up", modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        AuthModeHeader(mode)
        if (mode == AuthScreenMode.LOGIN) {
            LoginView(onSwitchToRegister = onSwitchMode, onLogin = { email, password -> session.login(email, password) }, isLoading = session.isLoading)
        } else {
            RegisterView(onSwitchToLogin = onSwitchMode, onRegister = { name, email, password -> session.register(name, email, password) }, isLoading = session.isLoading)
        }
    }
}

@Composable
private fun AuthModeHeader(mode: AuthScreenMode) {
    val items = if (mode == AuthScreenMode.LOGIN) listOf(
        Triple(Icons.Default.Description, "Continue your drafts", "Pick up your posts where you left off."),
        Triple(Icons.Default.People, "Rejoin the community", "Jump back into conversations and comments.")
    ) else listOf(
        Triple(Icons.Default.AutoAwesome, "Share ideas faster", "Create and publish your first post in minutes."),
        Triple(Icons.Default.PersonAdd, "Build your profile", "Connect with readers and grow your audience.")
    )
    Column(Modifier.padding(horizontal = 16.dp).fillMaxWidth().background(Color(0xFFF2F2F7), RoundedCornerShape(14.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(if (mode == AuthScreenMode.LOGIN) "Welcome back" else "Create your account", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        items.forEach { (icon, title, subtitle) -> HeaderItem(icon, title, subtitle) }
    }
}

@Composable
private fun HeaderItem(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = PostlyBlue, modifier = Modifier.size(22.dp)); Spacer(Modifier.width(10.dp)); Column { Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp); Text(subtitle, fontSize = 12.sp, color = Color.Gray) } }
}
