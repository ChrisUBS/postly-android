package com.example.postly.ui.views.posts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.postly.managers.SessionManager

/** Full create-post screen, including Pexels image suggestions driven by the title. */
@Composable
fun CreatePostView(
    session: SessionManager,
    onPostCreated: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    PostEditorView(
        mode = PostEditorMode.CREATE,
        session = session,
        onDone = onPostCreated,
        onCancel = onBack,
        modifier = modifier
    )
}
