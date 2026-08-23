package com.sudh.accord.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.sudh.accord.BuildConfig
import com.sudh.accord.navigation.Screen
import com.sudh.accord.viewmodel.AuthEvent
import com.sudh.accord.viewmodel.AuthUiState
import com.sudh.accord.viewmodel.AuthViewModel
import kotlinx.coroutines.launch



private const val WEB_CLIENT_ID = BuildConfig.GOOGLE_CLIENT_ID

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val teal = Color(0xFF0D9488)

    // ── Collect events, then check session ────────────────────────────────────
    // Collector is launched first so it's guaranteed to be registered before
    // checkExistingSession() can emit NavigateToHome.
    LaunchedEffect(Unit) {
        launch {
            authViewModel.events.collect { event ->
                when (event) {
                    is AuthEvent.NavigateToHome -> navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                    }
                    is AuthEvent.NavigateToOnboarding -> navController.navigate(Screen.OnboardingScreen.route) {
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                    }
                }
            }
        }
        authViewModel.checkExistingSession()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // ── Logo ──────────────────────────────────────────────────────────
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = teal,
                modifier = Modifier.size(68.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "A", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Accord",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "SPEND WITH INTENTION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
            )

            Spacer(modifier = Modifier.height(52.dp))

            // ── Google Sign In ────────────────────────────────────────────────
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        val credentialManager = CredentialManager.create(context)

                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setServerClientId(WEB_CLIENT_ID)
                            .setFilterByAuthorizedAccounts(false) // show all accounts, not just previously used
                            .build()

                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                        try {
                            val result = credentialManager.getCredential(context, request)
                            val credential = result.credential

                            if (credential is androidx.credentials.CustomCredential &&
                                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                            ) {
                                val googleIdTokenCredential =
                                    GoogleIdTokenCredential.createFrom(credential.data)
                                authViewModel.signInWithGoogle(googleIdTokenCredential.idToken)
                            } else {
                                Log.e("LoginScreen", "Unexpected credential type: ${credential.type}")
                            }
                        } catch (e: GetCredentialException) {
                            // TEMP: was a silent no-op — logging so we can see what's actually failing.
                            Log.e("LoginScreen", "Google credential retrieval failed: ${e::class.simpleName} — ${e.message}", e)
                        } catch (e: Exception) {
                            // Covers GoogleIdTokenCredential.createFrom() parsing failures too.
                            Log.e("LoginScreen", "Unexpected error during Google sign-in", e)
                        }
                    }
                },
                enabled = uiState !is AuthUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                if (uiState is AuthUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = teal
                    )
                } else {
                    Text("G", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Continue with Google", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                }
            }

            // ── Error message ─────────────────────────────────────────────────
            if (uiState is AuthUiState.Error) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = (uiState as AuthUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Divider ───────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "  OR  ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Email ─────────────────────────────────────────────────────────
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                placeholder = { Text("you@example.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = teal,
                    focusedLabelColor = teal,
                    cursorColor = teal
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ── Password ──────────────────────────────────────────────────────
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = teal,
                    focusedLabelColor = teal,
                    cursorColor = teal
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(
                            text = if (passwordVisible) "hide" else "show",
                            fontSize = 11.sp,
                            color = teal
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Forgot password ───────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = { /* TODO: Screen.ForgotPasswordScreen.route */ }) {
                    Text(text = "Forgot password?", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = teal)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Sign in (email/password — not wired yet) ──────────────────────
            Button(
                onClick = { /* TODO: wire up email/password auth */ },
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = teal,
                    contentColor = Color.White,
                    disabledContainerColor = teal.copy(alpha = 0.45f),
                    disabledContentColor = Color.White
                )
            ) {
                Text(text = "Sign in", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Sign up ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Text(
                    text = "Sign up",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = teal,
                    modifier = Modifier.clickable { /* TODO: Screen.SignUpScreen.route */ }
                )
            }
        }
    }
}