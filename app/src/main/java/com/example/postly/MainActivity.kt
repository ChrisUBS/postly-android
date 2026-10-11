package com.example.postly

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.postly.ui.PostlyApp
import com.example.postly.ui.theme.PostlyTheme

class MainActivity : ComponentActivity() {
    private var notificationPostId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        notificationPostId = intent.getStringExtra(EXTRA_NOTIFICATION_POST_ID)
        setContent { PostlyTheme { PostlyApp(notificationPostId) } }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationPostId = intent.getStringExtra(EXTRA_NOTIFICATION_POST_ID)
    }

    companion object {
        const val EXTRA_NOTIFICATION_POST_ID = "notificationPostId"
    }
}
