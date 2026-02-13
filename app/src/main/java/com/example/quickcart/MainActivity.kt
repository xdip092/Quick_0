package com.example.quickcart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.quickcart.ui.QuickCartApp
import com.example.quickcart.ui.theme.QuickCartTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuickCartTheme {
                QuickCartApp()
            }
        }
    }
}