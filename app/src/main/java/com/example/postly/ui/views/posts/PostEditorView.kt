package com.example.postly.ui.views.posts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.postly.managers.SessionManager
import com.example.postly.models.Post
import com.example.postly.models.PexelsPhoto
import com.example.postly.services.PexelsService
import com.example.postly.ui.theme.MutedText
import com.example.postly.ui.theme.PostlyBlue
import com.example.postly.ui.theme.PostlyText
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

enum class PostEditorMode { CREATE, EDIT }

@Composable
fun PostEditorView(mode: PostEditorMode, session: SessionManager, postId: String? = null, onDone: () -> Unit, onCancel: () -> Unit, modifier: Modifier = Modifier) {
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    var coverImage by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf("published") }
    var loadingPost by remember { mutableStateOf(mode == PostEditorMode.EDIT) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var suggestedImages by remember { mutableStateOf<List<PexelsPhoto>>(emptyList()) }
    var loadingImages by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val pexelsService = remember { PexelsService() }

    LaunchedEffect(postId, mode) {
        if (mode == PostEditorMode.EDIT && postId != null) {
            loadingPost = true
            runCatching { session.postService.getPostById(postId) }.onSuccess { post ->
                if (post.author.userId != session.user?.userId) error = "You don't have permission to edit this post."
                else { title = post.title; content = post.content; coverImage = post.coverImage.orEmpty(); status = post.status }
            }.onFailure { error = it.message ?: "Unable to load this post." }
            loadingPost = false
        }
    }
    LaunchedEffect(title) {
        val query = title.trim()
        if (query.isBlank()) { suggestedImages = emptyList(); return@LaunchedEffect }
        delay(500)
        loadingImages = true
        suggestedImages = runCatching { pexelsService.searchImages(query) }.getOrDefault(emptyList())
        loadingImages = false
    }

    Column(modifier.fillMaxSize().background(Color(0xFFF8F9FB))) {
        if (loadingPost) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = PostlyBlue) }
        else EditorContent(mode, title, content, coverImage, status, saving, error, suggestedImages, loadingImages, { title = it }, { content = it }, { coverImage = it }, { status = it }, onCancel = onCancel, onSave = {
            val cleanTitle = title.trim(); val cleanContent = content.trim()
            if (cleanTitle.isEmpty() || cleanContent.isEmpty()) {
                error = "Title and content are required."
            } else {
                scope.launch {
                    saving = true; error = null
                    val result = runCatching {
                        if (mode == PostEditorMode.CREATE) session.postService.createPost(cleanTitle, cleanContent, status, coverImage.trim().ifBlank { null })
                        else session.postService.updatePost(postId!!, cleanTitle, cleanContent, status, coverImage.trim().ifBlank { null })
                    }
                    result.onSuccess { onDone() }.onFailure { error = it.message ?: "Unable to save the post." }
                    saving = false
                }
            }
        })
    }
}

@Composable
private fun EditorContent(mode: PostEditorMode, title: String, content: String, coverImage: String, status: String, saving: Boolean, error: String?, suggestedImages: List<PexelsPhoto>, loadingImages: Boolean, onTitleChange: (String) -> Unit, onContentChange: (String) -> Unit, onCoverChange: (String) -> Unit, onStatusChange: (String) -> Unit, onCancel: () -> Unit, onSave: () -> Unit) {
    val words = content.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
    Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(if (mode == PostEditorMode.CREATE) Icons.Default.Create else Icons.Default.Edit, null, tint = PostlyBlue, modifier = Modifier.size(30.dp))
            Text(if (mode == PostEditorMode.CREATE) "Create new publication" else "Edit publication", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = PostlyText)
            Text(if (mode == PostEditorMode.CREATE) "Share your ideas with the Postly community" else "Update your Postly publication", color = MutedText, fontSize = 14.sp)
        }
        FieldLabel("Title")
        OutlinedTextField(title, onTitleChange, Modifier.fillMaxWidth(), placeholder = { Text("Write an attractive title...") }, singleLine = true, shape = RoundedCornerShape(10.dp), colors = editorFieldColors())
        FieldLabel("Cover image (optional)", Icons.Default.Image)
        if (title.isNotBlank()) {
            Text("Suggested images from Pexels", color = MutedText, fontSize = 14.sp)
            when {
                loadingImages -> Row(verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = PostlyBlue); Spacer(Modifier.width(8.dp)); Text("Loading images...", color = MutedText) }
                suggestedImages.isNotEmpty() -> PexelsSuggestions(suggestedImages, coverImage, onCoverChange)
                else -> Text("No images found. You can paste an image URL below.", color = MutedText, fontSize = 13.sp)
            }
        }
        OutlinedTextField(coverImage, onCoverChange, Modifier.fillMaxWidth(), placeholder = { Text("https://example.com/image.jpg") }, singleLine = true, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Uri), shape = RoundedCornerShape(10.dp), colors = editorFieldColors())
        coverImage.trim().takeIf { it.startsWith("http") }?.let { url -> Box(Modifier.fillMaxWidth().height(200.dp)) { AsyncImage(url, "Post cover preview", Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop); IconButton(onClick = { onCoverChange("") }, Modifier.align(Alignment.TopEnd).padding(6.dp).background(Color(0xFFFF3B30), RoundedCornerShape(30.dp))) { Icon(Icons.Default.Close, "Remove image", tint = Color.White) } } }
        FieldLabel("Content")
        OutlinedTextField(content, onContentChange, Modifier.fillMaxWidth().heightIn(min = 220.dp), placeholder = { Text("Write your publication...") }, shape = RoundedCornerShape(10.dp), colors = editorFieldColors())
        Row(Modifier.fillMaxWidth().background(Color(0xFFF2F2F7), RoundedCornerShape(12.dp)).padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) { Row { Icon(Icons.AutoMirrored.Filled.FormatAlignLeft, null, tint = MutedText); Spacer(Modifier.width(6.dp)); Text("Words: $words", color = MutedText) }; Row { Icon(Icons.Default.Schedule, null, tint = MutedText); Spacer(Modifier.width(6.dp)); Text("Reading: ~${maxOf(1, words / 200)} min", color = MutedText) } }
        FieldLabel("Status")
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) { StatusOption("published", "Post", status, onStatusChange); StatusOption("draft", "Save as draft", status, onStatusChange) }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onCancel, Modifier.weight(1f).height(52.dp), enabled = !saving, shape = RoundedCornerShape(12.dp)) { Text("Cancel") }
            Button(onSave, Modifier.weight(1f).height(52.dp), enabled = !saving, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = PostlyBlue, contentColor = Color.White)) { if (saving) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp) else { Icon(if (mode == PostEditorMode.CREATE) Icons.Default.Publish else Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text(if (status == "published") "Post" else "Save draft") } }
        }
    }
}

@Composable private fun FieldLabel(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) { Row(verticalAlignment = Alignment.CenterVertically) { icon?.let { Icon(it, null, tint = PostlyBlue); Spacer(Modifier.width(7.dp)) }; Text(text, fontWeight = FontWeight.Bold, color = PostlyText) } }
@Composable
private fun PexelsSuggestions(images: List<PexelsPhoto>, selectedUrl: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        images.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { image ->
                    OutlinedCard(onClick = { onSelect(image.largeUrl) }, modifier = Modifier.weight(1f), border = CardDefaults.outlinedCardBorder().copy(width = if (selectedUrl == image.largeUrl) 2.dp else 1.dp, brush = androidx.compose.ui.graphics.SolidColor(if (selectedUrl == image.largeUrl) PostlyBlue else Color(0xFFE0E0E0)))) {
                        Column { AsyncImage(image.mediumUrl, image.alt ?: "Pexels image", Modifier.fillMaxWidth().height(86.dp), contentScale = ContentScale.Crop); Text("By ${image.photographer}", Modifier.padding(6.dp), color = MutedText, maxLines = 1, fontSize = 10.sp) }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        Text("Images provided by Pexels", Modifier.fillMaxWidth(), color = MutedText, fontSize = 11.sp)
    }
}
@Composable private fun StatusOption(value: String, label: String, selected: String, onChange: (String) -> Unit) { Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = selected == value, onClick = { onChange(value) }); Text(label, color = PostlyText) } }
@Composable private fun editorFieldColors() = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color(0xFFF2F2F7), unfocusedContainerColor = Color(0xFFF2F2F7), focusedTextColor = PostlyText, unfocusedTextColor = PostlyText, focusedBorderColor = PostlyBlue, unfocusedBorderColor = Color.Transparent)
