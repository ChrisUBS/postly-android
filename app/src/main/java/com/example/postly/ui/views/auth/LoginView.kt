package com.example.postly.ui.views.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.postly.ui.theme.PostlyBlue
import kotlinx.coroutines.launch

@Composable
fun LoginView(onSwitchToRegister: () -> Unit, onLogin: suspend (String, String) -> String?, isLoading: Boolean, modifier: Modifier = Modifier) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        AuthTextField(email, { email = it }, "Email", Icons.Default.MailOutline)
        AuthTextField(password, { password = it }, "Password", Icons.Default.Lock, isPassword = true)
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
        Button(
            onClick = {
                errorMessage = if (email.isBlank() || password.isBlank()) "Please fill in all fields." else null
                if (errorMessage == null) scope.launch { errorMessage = onLogin(email.trim(), password) }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PostlyBlue, contentColor = Color.White, disabledContentColor = Color.White.copy(alpha = .7f)),
            enabled = !isLoading
        ) {
            if (isLoading) CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp) else Text("Log In", fontSize = 17.sp)
        }
        TextButton(onClick = onSwitchToRegister, modifier = Modifier.fillMaxWidth()) { Text("Don't have an account? Register") }
    }
}

@Composable
internal fun AuthTextField(value: String, onValueChange: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isPassword: Boolean = false) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(), label = { Text(label) },
        leadingIcon = { Icon(icon, null) }, singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(10.dp), colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF2F2F7), unfocusedContainerColor = Color(0xFFF2F2F7),
            focusedTextColor = Color(0xFF333333), unfocusedTextColor = Color(0xFF333333),
            focusedLabelColor = PostlyBlue, unfocusedLabelColor = Color(0xFF666666),
            cursorColor = PostlyBlue
        )
    )
}
