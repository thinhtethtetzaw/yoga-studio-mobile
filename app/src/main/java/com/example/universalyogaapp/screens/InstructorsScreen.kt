package com.example.universalyogaapp.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.universalyogaapp.Routes
import com.example.universalyogaapp.components.CommonScaffold
import com.example.universalyogaapp.models.Instructor
import com.example.universalyogaapp.viewmodels.InstructorViewModel
import kotlinx.coroutines.flow.StateFlow

@Composable
fun InstructorsScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: InstructorViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as android.app.Application)
    )
    
    val instructors by viewModel.instructors.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        viewModel.loadInstructors()
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var instructorToDelete by remember { mutableStateOf<Instructor?>(null) }

    var showEditDialog by remember { mutableStateOf(false) }
    var instructorToEdit by remember { mutableStateOf<Instructor?>(null) }
    var editName by remember { mutableStateOf("") }
    var editExperience by remember { mutableStateOf("") }
    var showEditError by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false 
                instructorToDelete = null
            },
            title = { Text("Delete Instructor") },
            text = { Text("Are you sure you want to delete this instructor?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        instructorToDelete?.let { instructor ->
                            viewModel.deleteInstructor(instructor.id)
                        }
                        showDeleteDialog = false
                        instructorToDelete = null
                    }
                ) {
                    Text("Delete", color = Color(0xFFB00020))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteDialog = false
                        instructorToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { 
                showEditDialog = false 
                instructorToEdit = null
                showEditError = false
            },
            title = { Text("Edit Instructor") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = showEditError && editName.isBlank()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = editExperience,
                        onValueChange = { 
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                editExperience = it
                            }
                        },
                        label = { Text("Experience (years)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        isError = showEditError && editExperience.isBlank()
                    )

                    if (showEditError) {
                        Text(
                            text = "Please fill all fields",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editName.isBlank() || editExperience.isBlank()) {
                            showEditError = true
                            return@TextButton
                        }
                        
                        instructorToEdit?.let { instructor ->
                            viewModel.updateInstructor(
                                instructor.copy(
                                    name = editName,
                                    experience = editExperience
                                )
                            )
                        }
                        showEditDialog = false
                        instructorToEdit = null
                        showEditError = false
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showEditDialog = false
                        instructorToEdit = null
                        showEditError = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    CommonScaffold(
        navController = navController
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Instructors",
                    style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.DarkGray
                )
                
                OutlinedButton(
                    onClick = { navController.navigate(Routes.CreateInstructor.route) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.secondary
                    ),
                    shape = MaterialTheme.shapes.small
                ) {
                   Text("+ Add", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyLarge)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(instructors) { instructor ->
                    var showMenu by remember { mutableStateOf(false) }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Smaller icon container
                            Surface(
                                modifier = Modifier.size(48.dp),
                                shape = MaterialTheme.shapes.medium,
                                color = Color.White
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Instructor Icon",
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .size(28.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = instructor.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Experience: ${instructor.experience} years",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.Gray
                                )
                            }
                            
                            Box {
                                IconButton(
                                    onClick = { showMenu = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More options",
                                        tint = Color.Gray
                                    )
                                }
                                
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    DropdownMenuItem(
                                        text = { 
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit",
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    "Edit",
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        },
                                        onClick = {
                                            showMenu = false
                                            instructorToEdit = instructor
                                            editName = instructor.name
                                            editExperience = instructor.experience
                                            showEditDialog = true
                                        },
                                        modifier = Modifier.height(32.dp)
                                    )
                                    DropdownMenuItem(
                                        text = { 
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = Color(0xFFB00020),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    "Delete",
                                                    color = Color(0xFFB00020)
                                                )
                                            }
                                        },
                                        onClick = {
                                            showMenu = false
                                            instructorToDelete = instructor
                                            showDeleteDialog = true
                                        },
                                        modifier = Modifier.height(32.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
