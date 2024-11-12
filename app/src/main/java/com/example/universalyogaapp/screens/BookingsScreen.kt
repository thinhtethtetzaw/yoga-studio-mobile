package com.example.universalyogaapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.universalyogaapp.components.CommonScaffold

@Composable
fun BookingsScreen(navController: NavController) {
    CommonScaffold(
        navController = navController,
        title = "Bookings"
    ) { paddingValues ->
        // Add your bookings screen content here
    }
} 