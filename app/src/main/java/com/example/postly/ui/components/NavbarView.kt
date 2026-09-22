package com.example.postly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.postly.ui.theme.MutedText
import com.example.postly.ui.theme.PostlyBlue
import com.example.postly.ui.theme.PostlyText

@Composable
fun NavbarView(isSearchExpanded: Boolean, query: String, isMenuOpen: Boolean, onQueryChange: (String) -> Unit, onSearchToggle: () -> Unit, onSearch: () -> Unit, onMenuToggle: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().background(Color.White.copy(alpha = .95f)).padding(start = 16.dp, end = 8.dp, top = 34.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.AutoMirrored.Filled.Chat, null, tint = PostlyBlue, modifier = Modifier.size(25.dp)); Spacer(Modifier.width(8.dp)); Text("Postly", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PostlyText) }
        Spacer(Modifier.weight(1f))
        if (isSearchExpanded) {
            OutlinedTextField(query, onQueryChange, Modifier.widthIn(max = 320.dp).weight(1f, false), placeholder = { Text("Search posts...") }, leadingIcon = { Icon(Icons.Default.Search, null, tint = MutedText) }, trailingIcon = { IconButton(onSearchToggle) { Icon(Icons.Default.Close, "Close search", tint = MutedText) } }, singleLine = true, shape = RoundedCornerShape(14.dp))
            IconButton(onSearch) { Icon(Icons.AutoMirrored.Filled.Send, "Search", tint = PostlyBlue) }
        } else IconButton(onSearchToggle, Modifier.clip(CircleShape).background(Color(0x26000000))) { Icon(Icons.Default.Search, "Search", tint = MutedText) }
        IconButton(onMenuToggle) { Icon(if (isMenuOpen) Icons.Default.Close else Icons.Default.Menu, "Menu", tint = MutedText) }
    }
}
