package com.example.postly.ui.views.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.postly.ui.theme.PostlyBlue

@Composable
fun RegisterView(onSwitchToLogin: () -> Unit, onRegister: (String, String, String) -> Unit, modifier: Modifier = Modifier) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }

    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        AuthTextField(name, { name = it }, "Full Name", Icons.Default.Person)
        AuthTextField(email, { email = it }, "Email", Icons.Default.MailOutline)
        AuthTextField(password, { password = it }, "Password", Icons.Default.Lock, isPassword = true)
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
        Button(
            onClick = {
                errorMessage = if (name.isBlank() || email.isBlank() || password.isBlank()) "Please fill in all fields." else null
                if (errorMessage == null) onRegister(name.trim(), email.trim(), password)
            },
            modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PostlyBlue)
        ) { Text("Register", fontSize = 17.sp) }
        TextButton(onClick = onSwitchToLogin, modifier = Modifier.fillMaxWidth()) { Text("Already have an account? Log In") }
    }
}
