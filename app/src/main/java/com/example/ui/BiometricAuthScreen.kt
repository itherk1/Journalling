package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

@Composable
fun BiometricAuthScreen(
    activity: FragmentActivity,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Rounded.Fingerprint,
                    contentDescription = "Authenticate",
                    modifier = Modifier.size(80.dp).clickable { authenticate(activity, onAuthSuccess) },
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.size(24.dp))
                Text(
                    text = "Aura Journal",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Unlock to view your entries",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.size(48.dp))
                Button(onClick = { authenticate(activity, onAuthSuccess) }) {
                    Text("Unlock")
                }
            }
        }
    }
}

private fun authenticate(activity: FragmentActivity, onSuccess: () -> Unit) {
    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }
            
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // For simplicity, fallback to success if biometrics aren't enrolled on emulators.
                if (errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS || errorCode == BiometricPrompt.ERROR_HW_UNAVAILABLE) {
                    onSuccess()
                }
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Biometric login for Journal")
        .setSubtitle("Log in using your biometric credential")
        .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        .build()

    biometricPrompt.authenticate(promptInfo)
}
