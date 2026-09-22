package com.example.postly.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.postly.ui.theme.MutedText
import com.example.postly.ui.theme.PostlyText

@Composable fun FeatureCard(icon: ImageVector, iconColor: Color, title: String, description: String, modifier: Modifier = Modifier) { Card(modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(16.dp)), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) { Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) { Icon(icon, null, tint = iconColor, modifier = Modifier.size(40.dp)); Text(title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = PostlyText); Text(description, color = MutedText, textAlign = TextAlign.Center, lineHeight = 21.sp) } } }
