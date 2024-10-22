package com.example.universalyogaapp

sealed class Routes(val route: String) {
    object Login : Routes("login")
    object Register : Routes("register")
    object Intro : Routes("intro")
    object Home : Routes("home")
    // ... (other routes)
}
