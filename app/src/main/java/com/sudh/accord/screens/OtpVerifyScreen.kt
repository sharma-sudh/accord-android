package com.sudh.accord.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sudh.accord.navigation.Screen
import com.sudh.accord.viewmodel.ForgotPasswordEvent
import com.sudh.accord.viewmodel.ForgotPasswordUiState
import com.sudh.accord.viewmodel.ForgotPasswordViewModel

private const val OTP_LENGTH = 6

@Composable
fun OtpVerifyScreen(
    navController: NavController,
    viewModel: ForgotPasswordViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var otp by remember { mutableStateOf("") }

    val teal = MaterialTheme.colorScheme.primary

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ForgotPasswordEvent.OtpVerified ->
                    navController.navigate(Screen.ResetPasswordScreen.route)
                is ForgotPasswordEvent.OtpSent ->
                    otp = "" // resend landed — let them re-enter cleanly
                else -> Unit
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
            Spacer(modifier = Modifier.height(20.dp))

            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Enter the code",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "We sent a ${OTP_LENGTH}-digit code to ${viewModel.email}. It expires in 10 minutes.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= OTP_LENGTH && it.all(Char::isDigit)) otp = it },
                label = { Text("Code") },
                placeholder = { Text("000000") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
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
                onClick = { viewModel.verifyOtp(otp) },
                enabled = uiState !is ForgotPasswordUiState.Loading && otp.length == OTP_LENGTH,
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
                    Text(text = "Verify", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Didn't get a code? ",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                TextButton(
                    onClick = { viewModel.resendOtp() },
                    enabled = uiState !is ForgotPasswordUiState.Loading
                ) {
                    Text(text = "Resend", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = teal)
                }
            }
        }
    }
}