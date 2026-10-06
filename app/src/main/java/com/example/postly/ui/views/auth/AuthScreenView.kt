package com.example.postly.ui.views.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.postly.BuildConfig
import com.example.postly.ui.theme.PostlyBlue
import com.example.postly.managers.SessionManager
import com.example.postly.services.GoogleSignInService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class AuthScreenMode { LOGIN, REGISTER }

@Composable
fun AuthScreenView(
    mode: AuthScreenMode,
    session: SessionManager,
    onSwitchMode: () -> Unit,
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(session.user) { if (session.isAuthenticated) onAuthenticated() }
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val googleSignInService = remember(activity) { activity?.let(::GoogleSignInService) }
    val scope = rememberCoroutineScope()
    var isSigningInWithGoogle by remember { mutableStateOf(false) }
    var googleError by remember { mutableStateOf<String?>(null) }
    var googleStep by remember { mutableStateOf("Waiting for Google…") }
    val isAuthLoading = isSigningInWithGoogle || session.isLoading

    Column(modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState()).padding(top = 20.dp)) {
        Text(if (mode == AuthScreenMode.LOGIN) "Log In" else "Sign Up", modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp), fontSize = 28.sp, fontWeight = FontWeight.Bold)
        AuthModeHeader(mode)
        Spacer(Modifier.height(16.dp))
        GoogleSignInButton(
            isLoading = isAuthLoading,
            loadingText = googleStep,
            onClick = {
                googleError = null
                googleStep = "Waiting for Google…"
                scope.launch {
                    isSigningInWithGoogle = true
                    try {
                        val signInService = googleSignInService
                            ?: error("Could not start Google sign-in from this screen.")
                        val idToken = signInService.getIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                        googleStep = "Signing in to Postly…"
                        googleError = session.loginWithGoogle(idToken)?.let(::formatGoogleSignInError)
                    } catch (_: GetCredentialCancellationException) {
                        googleError = null
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Exception) {
                        googleError = formatGoogleSignInError(
                            error.message?.takeIf { it.isNotBlank() }
                                ?: "Could not sign in with Google. Please try again."
                        )
                    } finally {
                        isSigningInWithGoogle = false
                    }
                }
            }
        )
        googleError?.let {
            Text(it, Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }
        AuthDivider()
        if (mode == AuthScreenMode.LOGIN) {
            LoginView(onSwitchToRegister = onSwitchMode, onLogin = { email, password -> session.login(email, password) }, isLoading = isAuthLoading)
        } else {
            RegisterView(onSwitchToLogin = onSwitchMode, onRegister = { name, email, password -> session.register(name, email, password) }, isLoading = isAuthLoading)
        }
    }
}

@Composable
private fun GoogleSignInButton(isLoading: Boolean, loadingText: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp),
        enabled = !isLoading,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFDADCE0)),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color(0xFF3C4043))
    ) {
        if (isLoading) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = PostlyBlue)
            Spacer(Modifier.width(10.dp))
            Text(loadingText)
        } else {
            Text("G", color = Color(0xFF4285F4), fontSize = 19.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(10.dp))
            Text("Continue with Google", fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun AuthDivider() {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(1f).height(1.dp).background(Color(0xFFE5E5EA)))
        Text("OR CONTINUE WITH EMAIL", Modifier.padding(horizontal = 12.dp), color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Box(Modifier.weight(1f).height(1.dp).background(Color(0xFFE5E5EA)))
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

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun formatGoogleSignInError(message: String): String {
    val detail = runCatching {
        JSONObject(message).optString("error").takeIf { it.isNotBlank() }
    }.getOrNull() ?: message
    val likelyAudienceMismatch = detail.contains("audience", ignoreCase = true) ||
        detail.contains("client id", ignoreCase = true)
    return if (likelyAudienceMismatch) {
        "$detail Check that GOOGLE_WEB_CLIENT_ID in the app matches GOOGLE_CLIENT_ID on the backend."
    } else {
        detail
    }
}
