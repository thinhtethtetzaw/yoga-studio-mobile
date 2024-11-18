package com.example.universalyogaapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.example.universalyogaapp.components.CommonScaffold
import com.example.universalyogaapp.viewmodels.InstructorViewModel

@Composable
fun CreateInstructorScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: InstructorViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as android.app.Application)
    )
    
    var name by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    CommonScaffold(
        navController = navController,
        title = "Create Instructor"
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                isError = showError && name.isBlank()
            )
            
            OutlinedTextField(
                value = experience,
                onValueChange = { 
                    if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                        experience = it
                    }
                },
                label = { Text("Experience (years)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = showError && experience.isBlank()
            )

            if (showError) {
                Text(
                    text = "Please fill all fields",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Button(
                onClick = {
                    if (name.isBlank() || experience.isBlank()) {
                        showError = true
                        return@Button
                    }
                    
                    isLoading = true
                    viewModel.addInstructor(name, experience) { success ->
                        isLoading = false
                        if (success) {
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape =  RoundedCornerShape(6.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Create Instructor")
                }
            }
        }
    }
} 