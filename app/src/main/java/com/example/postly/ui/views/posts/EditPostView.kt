package com.example.postly.ui.views.posts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.postly.managers.SessionManager

/** Full edit-post screen, including Pexels image suggestions driven by the edited title. */
@Composable
fun EditPostView(
    postId: String,
    session: SessionManager,
    onPostUpdated: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    PostEditorView(
        mode = PostEditorMode.EDIT,
        session = session,
        postId = postId,
        onDone = onPostUpdated,
        onBack = onBack,
        modifier = modifier
    )
}
