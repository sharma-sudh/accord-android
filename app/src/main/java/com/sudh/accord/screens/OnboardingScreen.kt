package com.sudh.accord.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sudh.accord.components.AddTaskSheet
import com.sudh.accord.model.Task
import com.sudh.accord.navigation.Screen

@Composable
fun OnboardingScreen(navController: NavController) {

    val teal = Color(0xFF0D9488)

    var budget          by remember { mutableStateOf("") }
    var budgetError     by remember { mutableStateOf(false) }
    var pendingTask     by remember { mutableStateOf<Task?>(null) }
    var showTaskSheet   by remember { mutableStateOf(false) }

    if (showTaskSheet) {
        AddTaskSheet(
             onDismiss = { showTaskSheet = false },
             onTaskAdded = { task -> pendingTask = task; showTaskSheet = false }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))

            // ── Header ────────────────────────────────────────────────────────
            Text(
                text = "Set your budget",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "We'll build your spending wallet around it.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
            )

            Spacer(Modifier.height(40.dp))

            // ── Monthly Budget (mandatory) ────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Monthly Budget",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = budget,
                    onValueChange = {
                        budget = it.filter { c -> c.isDigit() || c == '.' }
                        budgetError = false
                    },
                    prefix = { Text("₹", fontWeight = FontWeight.Medium) },
                    placeholder = { Text("0.00") },
                    isError = budgetError,
                    supportingText = if (budgetError) {
                        { Text("Please enter a valid budget", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = teal,
                        focusedLabelColor = teal,
                        cursorColor = teal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── Optional task ─────────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "First Task",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "Optional",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))

                AnimatedContent(
                    targetState = pendingTask,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "task_slot"
                ) { task ->
                    if (task == null) {
                        // Dashed dropzone
                        val outlineColor = MaterialTheme.colorScheme.outline
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .drawBehind {
                                    drawRoundRect(
                                        color = outlineColor,
                                        style = Stroke(
                                            width = 1.5.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(
                                                intervals = floatArrayOf(10f, 7f),
                                                phase = 0f
                                            )
                                        ),
                                        cornerRadius = CornerRadius(12.dp.toPx())
                                    )
                                }
                                .clickable(role = Role.Button) { showTaskSheet = true }
                                .padding(vertical = 28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "Add your first task",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // Task pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = teal.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "₹%.0f reward".format(task.value),
                                        fontSize = 12.sp,
                                        color = teal,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                IconButton(onClick = { pendingTask = null }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Close,
                                        contentDescription = "Remove task",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            // ── Get Started ───────────────────────────────────────────────────
            Button(
                onClick = {
                    val parsed = budget.toDoubleOrNull()
                    if (parsed == null || parsed <= 0.0) {
                        budgetError = true
                    } else {
                        // TODO: persist budget (and pendingTask if set) to your repo
                        navController.navigate(Screen.HomeScreen.route) {
                            popUpTo(Screen.LoginScreen.route) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = teal,
                    contentColor = Color.White
                )
            ) {
                Text(text = "Get Started", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}