package com.unitbv.speedy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.unitbv.speedy.screens.LoginScreen
import com.unitbv.speedy.screens.SignUpScreen
import com.unitbv.speedy.ui.theme.SpeedyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpeedyTheme {
                var currentScreen by remember { mutableStateOf("login") }

                when (currentScreen) {
                    "login" -> LoginScreen(
                        onLoginClick = { email, password ->
                            // TODO: logica de autentificare
                        },
                        onSignUpClick = { currentScreen = "signup" },
                        onForgotPasswordClick = { }
                    )
                    "signup" -> SignUpScreen(
                        onSignUpClick = { name, email, password ->
                            // TODO: logica de înregistrare
                        },
                        onLoginClick = { currentScreen = "login" }
                    )
                }
            }
        }
    }
}