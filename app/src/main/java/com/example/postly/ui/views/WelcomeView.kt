package com.example.postly.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.postly.ui.components.FeatureCard
import com.example.postly.ui.theme.*

@Composable
fun WelcomeView(onJoin: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().background(Brush.linearGradient(listOf(WelcomeTop, WelcomeBottom))).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) { Icon(Icons.AutoMirrored.Filled.Chat, null, tint = PostlyBlue, modifier = Modifier.size(50.dp)); Spacer(Modifier.width(12.dp)); Text("Welcome to Postly", color = PostlyText, fontSize = 34.sp, fontWeight = FontWeight.Bold) }
        Text("A platform to share ideas, connect with people, and explore fascinating conversations all in one place.", Modifier.widthIn(max = 500.dp), MutedText, 18.sp, textAlign = TextAlign.Center, lineHeight = 25.sp)
        Spacer(Modifier.height(12.dp))
        FeatureCard(Icons.Default.Create, PostlyBlue, "Create Posts", "Share your thoughts, stories, and ideas with our community.")
        FeatureCard(Icons.Default.Forum, Color(0xFF34C759), "Explore Conversations", "Discover amazing posts and participate in relevant conversations.")
        FeatureCard(Icons.Default.People, Color(0xFFAF52DE), "Connect", "Meet people with similar interests and expand your network.")
        Spacer(Modifier.height(12.dp))
        Button(onJoin, Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = PostlyBlue)) { Icon(Icons.Default.Create, null); Spacer(Modifier.width(8.dp)); Text("Join Now", fontSize = 18.sp) }
    }
}
