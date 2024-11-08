package com.example.universalyogaapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.universalyogaapp.components.CommonScaffold
import com.example.universalyogaapp.DatabaseHelper
import com.example.universalyogaapp.models.Instructor
import com.example.universalyogaapp.viewmodels.InstructorViewModel
import com.example.universalyogaapp.viewmodels.CourseViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TextButton
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import com.example.universalyogaapp.viewmodels.ClassViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClassScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dbHelper = remember { DatabaseHelper(context) }
    val instructorViewModel: InstructorViewModel = viewModel()
    val courseViewModel: CourseViewModel = viewModel()
    val classViewModel: ClassViewModel = viewModel()

    var className by remember { mutableStateOf("") }
    var instructorName by remember { mutableStateOf("") }
    var courseName by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("") }
    var selectedDayOfWeek by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    
    var courseExpanded by remember { mutableStateOf(false) }
    var instructorExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Collect instructors from ViewModel
    val instructors by instructorViewModel.instructors.collectAsState()
    // Collect courses from CourseViewModel
    val coursesWithCount by courseViewModel.coursesWithCount.collectAsState()

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        
                        // Format the date
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        selectedDate = date.format(formatter)
                        
                        // Get day of week
                        selectedDayOfWeek = date.dayOfWeek.getDisplayName(
                            TextStyle.FULL,
                            Locale.getDefault()
                        )
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    CommonScaffold(
        navController = navController,
        title = "Add New Class"
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = className,
                onValueChange = { className = it },
                label = { Text("Class Name") },
                modifier = Modifier.fillMaxWidth()
            )

            // Instructor Dropdown
            ExposedDropdownMenuBox(
                expanded = instructorExpanded,
                onExpandedChange = { instructorExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = instructorName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Instructor") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = instructorExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = instructorExpanded,
                    onDismissRequest = { instructorExpanded = false }
                ) {
                    instructors.forEach { instructor ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = instructor.name,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            onClick = {
                                instructorName = instructor.name
                                instructorExpanded = false
                            }
                        )
                    }
                }
            }

            // Course Dropdown
            ExposedDropdownMenuBox(
                expanded = courseExpanded,
                onExpandedChange = { courseExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = courseName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Course") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = courseExpanded,
                    onDismissRequest = { courseExpanded = false }
                ) {
                    coursesWithCount.forEach { courseWithCount ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = courseWithCount.course.courseName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            onClick = {
                                courseName = courseWithCount.course.courseName
                                courseExpanded = false
                            }
                        )
                    }
                }
            }

            // Date Field with Date Picker
            OutlinedTextField(
                value = if (selectedDate.isNotEmpty()) {
                    "$selectedDayOfWeek, $selectedDate"
                } else {
                    ""
                },
                onValueChange = { },
                readOnly = true,
                label = { Text("Select Date") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select date"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Comment (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            if (showError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            Button(
                onClick = {
                    when {
                        className.isBlank() -> {
                            showError = true
                            errorMessage = "Please enter class name"
                        }
                        instructorName.isBlank() -> {
                            showError = true
                            errorMessage = "Please select an instructor"
                        }
                        courseName.isBlank() -> {
                            showError = true
                            errorMessage = "Please select a course"
                        }
                        selectedDate.isBlank() -> {
                            showError = true
                            errorMessage = "Please select a date"
                        }
                        else -> {
                            showError = false
                            try {
                                classViewModel.addClass(
                                    name = className.trim(),
                                    instructorName = instructorName.trim(),
                                    courseName = courseName.trim(),
                                    date = selectedDate.trim(),
                                    comment = comment.trim()
                                )
                                navController.navigateUp()
                            } catch (e: Exception) {
                                showError = true
                                errorMessage = "Error adding class: ${e.message}"
                                e.printStackTrace()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Add Class")
            }
        }
    }
} 