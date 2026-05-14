package com.sudh.accord.screens

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.sudh.accord.navigation.Screen

@Composable
fun LoginScreen(navController: NavController) {
    Text("Hello from Login")
    Button(onClick = { navController.navigate(Screen.HomeScreen.route) }) {
        Text("Go to Home")
    }
}