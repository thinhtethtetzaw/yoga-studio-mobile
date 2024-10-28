package com.example.universalyogaapp

sealed class Routes(val route: String) {
    object Login : Routes("login")
    object Register : Routes("register")
    object Intro : Routes("intro")
    object Home : Routes("home")
    object Courses : Routes("courses")
    object Classes : Routes("classes")
    object Instructors : Routes("instructors")
    object Participants : Routes("participants")
    object Profile : Routes("profile")
    // ... (other routes)
}
