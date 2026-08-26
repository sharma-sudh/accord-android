package com.sudh.accord.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sudh.accord.navigation.Screen
import com.sudh.accord.viewmodel.ForgotPasswordEvent
import com.sudh.accord.viewmodel.ForgotPasswordUiState
import com.sudh.accord.viewmodel.ForgotPasswordViewModel

private const val MIN_PASSWORD_LENGTH = 8

@Composable
fun ResetPasswordScreen(
    navController: NavController,
    viewModel: ForgotPasswordViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var newPassword     by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showValidationErrors by remember { mutableStateOf(false) }

    val teal = MaterialTheme.colorScheme.primary

    val lengthError = showValidationErrors && newPassword.length < MIN_PASSWORD_LENGTH
    val matchError  = showValidationErrors && confirmPassword != newPassword

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is ForgotPasswordEvent.PasswordResetDone) {
                navController.navigate(Screen.LoginScreen.route) {
                    popUpTo(Screen.LoginScreen.route) { inclusive = true }
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "Set a new password",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose a new password for your account.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = lengthError,
                supportingText = {
                    if (lengthError) Text("At least $MIN_PASSWORD_LENGTH characters")
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
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

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = matchError,
                supportingText = {
                    if (matchError) Text("Passwords don't match")
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = teal,
                    focusedLabelColor = teal,
                    cursorColor = teal
                )
            )

            if (uiState is ForgotPasswordUiState.Error) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = (uiState as ForgotPasswordUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    showValidationErrors = true
                    val isValid = newPassword.length >= MIN_PASSWORD_LENGTH &&
                            confirmPassword == newPassword
                    if (isValid) viewModel.resetPassword(newPassword)
                },
                enabled = uiState !is ForgotPasswordUiState.Loading,
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
                if (uiState is ForgotPasswordUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(text = "Reset password", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}