package com.sudh.accord

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sudh.accord.navigation.NavGraph
import com.sudh.accord.ui.theme.AccordTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AccordTheme {
                NavGraph()
            }
        }
    }
}