package com.example.universalyogaapp.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.foundation.layout.padding
import com.example.universalyogaapp.components.CommonScaffold

@Composable
fun ParticipantsScreen(navController: NavController) {
    CommonScaffold(navController = navController, title = "Participant") { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Participants Screen")
        }
    }
}
