package com.example.postly.services

import android.content.Context
import android.content.MutableContextWrapper
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class GoogleSignInService(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)
    private val mutableContext = MutableContextWrapper(context)

    suspend fun getIdToken(serverClientId: String): String {
        require(serverClientId.isNotBlank()) {
            "Set GOOGLE_WEB_CLIENT_ID in secrets.properties to enable Google sign-in."
        }

        val option = GetSignInWithGoogleOption.Builder(serverClientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
        val result = credentialManager.getCredential(context = mutableContext, request = request)
        val data = (result.credential as? CustomCredential)
            ?.takeIf { it.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL }
            ?.data
            ?: error("Google did not return a valid sign-in credential.")

        return GoogleIdTokenCredential.createFrom(data).idToken
    }
}
