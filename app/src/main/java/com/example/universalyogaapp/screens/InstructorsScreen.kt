package com.example.universalyogaapp.screens

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.window.DialogProperties
import com.example.universalyogaapp.Routes
import com.example.universalyogaapp.components.CommonScaffold
import com.example.universalyogaapp.models.Instructor
import com.example.universalyogaapp.viewmodels.InstructorViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Log
import com.example.universalyogaapp.components.NetworkStatusBar

@Composable
fun InstructorsScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: InstructorViewModel = viewModel(
        factory = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as android.app.Application)
    )
    
    val instructors by viewModel.instructors.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        viewModel.loadLocalInstructors()
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var instructorToDelete by remember { mutableStateOf<Instructor?>(null) }

    var showEditDialog by remember { mutableStateOf(false) }
    var instructorToEdit by remember { mutableStateOf<Instructor?>(null) }
    var editName by remember { mutableStateOf("") }
    var editExperience by remember { mutableStateOf("") }
    var showEditError by remember { mutableStateOf(false) }

    var showSyncDialog by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }

    var showNetworkStatus by remember { mutableStateOf(false) }
    var networkStatusTimer: Job? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false 
                instructorToDelete = null
            },
            title = { Text("Are you sure to delete this instructor?") },
            confirmButton = {},
            dismissButton = {},
            containerColor = Color.White,
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(8.dp),
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.padding(horizontal = 16.dp),
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { 
                                showDeleteDialog = false
                                instructorToDelete = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF5F5F5)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Cancel",
                                color = Color.Black
                            )
                        }
                        
                        Button(
                            onClick = {
                                instructorToDelete?.let { instructor ->
                                    viewModel.deleteInstructor(instructor.id)
                                }
                                showDeleteDialog = false
                                instructorToDelete = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE57373)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Confirm",
                                color = Color.White
                            )
                        }
                    }
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
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = showEditError && editName.isBlank()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
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

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { 
                                showEditDialog = false
                                instructorToEdit = null
                                showEditError = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF5F5F5)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Cancel",
                                color = Color.Black
                            )
                        }
                        
                        Button(
                            onClick = {
                                if (editName.isBlank() || editExperience.isBlank()) {
                                    showEditError = true
                                    return@Button
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
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Save",
                                color = Color.White
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {},
            containerColor = Color.White,
            tonalElevation = 8.dp,
            shape = RoundedCornerShape(8.dp),
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp)
        )
    }

    CommonScaffold(
        navController = navController,
        title = "Instructor",
        actions = {
            // Sync Button
            IconButton(
                onClick = {
                    showNetworkStatus = true
                    networkStatusTimer?.cancel()
                    networkStatusTimer = scope.launch {
                        if (isNetworkAvailable(context)) {
                            isSyncing = true
                            try {
                                viewModel.syncWithFirebase { success ->
                                    isSyncing = false
                                    showSyncDialog = false
                                }
                                // Hide network status after successful sync
                                delay(2000) // Show for 2 seconds
                                showNetworkStatus = false
                            } catch (e: Exception) {
                                Log.e("InstructorsScreen", "Sync error", e)
                                showSyncDialog = true
                            } finally {
                                isSyncing = false
                            }
                        } else {
                            showSyncDialog = true
                            // Keep network status visible for error state
                            delay(3000) // Show for 3 seconds
                            showNetworkStatus = false
                        }
                    }
                },
                modifier = Modifier.size(48.dp)
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync instructors",
                        tint = if (isNetworkAvailable(context)) 
                            MaterialTheme.colorScheme.secondary 
                        else 
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Add Button
            OutlinedButton(
                onClick = { navController.navigate(Routes.CreateInstructor.route) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(end = 16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("+ Add", color = MaterialTheme.colorScheme.secondary)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NetworkStatusBar(
                isOnline = isNetworkAvailable(context),
                visible = showNetworkStatus
            )

            Text(
                text = "Instructors",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                modifier = Modifier.padding(16.dp)
            )

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
                                color = MaterialTheme.colorScheme.background,
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
                                    style = MaterialTheme.typography.titleMedium,
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

private fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
           capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}
