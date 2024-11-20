package com.example.universalyogaapp.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.universalyogaapp.components.CommonScaffold
import com.example.universalyogaapp.viewmodels.InstructorViewModel
import com.example.universalyogaapp.viewmodels.CourseViewModel
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TextButton
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import com.example.universalyogaapp.viewmodels.ClassViewModel
import com.example.universalyogaapp.components.DatePickerField
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClassScreen(navController: NavController) {
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

    var selectedCourseId by remember { mutableStateOf(0L) }

    var showConfirmationDialog by remember { mutableStateOf(false) }

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

    LaunchedEffect(Unit) {
        courseViewModel.loadCourses()
    }

    LaunchedEffect(coursesWithCount) {
        println("Courses loaded: ${coursesWithCount.size}")
        coursesWithCount.forEach { 
            println("Course: ${it.course.courseName}")
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
                                selectedCourseId = courseWithCount.course.id
                                Log.d("AddClassScreen", "Selected course: ${courseWithCount.course.courseName} with ID: ${courseWithCount.course.id}")
                                courseExpanded = false
                            }
                        )
                    }
                    if (coursesWithCount.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No courses available") },
                            onClick = { }
                        )
                    }
                }
            }

            // Date Field with Date Picker
            DatePickerField(
                value = selectedDate,
                onDateSelected = { selectedDate = it },
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
                            showConfirmationDialog = true
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

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            containerColor = Color.White,
            shape = RoundedCornerShape(8.dp),
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Are you sure you want to create this class?",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DetailRow("Class Name:", className)
                        DetailRow("Instructor:", instructorName)
                        DetailRow("Course:", courseName)
                        DetailRow("Date:", formatDate(selectedDate))
                        if (comment.isNotBlank()) {
                            DetailRow("Comment:", comment)
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { showConfirmationDialog = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Button(
                        onClick = {
                            try {
                                val courseIdLong = selectedCourseId.toLong()
                                classViewModel.addClass(
                                    name = className.trim(),
                                    instructorName = instructorName.trim(),
                                    courseId = courseIdLong,
                                    courseName = courseName.trim(),
                                    date = selectedDate.trim(),
                                    comment = comment.trim()
                                )
                                showConfirmationDialog = false
                                navController.navigateUp()
                            } catch (e: Exception) {
                                Log.e("AddClassScreen", "Error in onClick: ", e)
                                showError = true
                                errorMessage = "Error adding class: ${e.message}"
                                e.printStackTrace()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Confirm")
                    }
                }
            },
            dismissButton = null
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1.5f)
        )
    }
}

// Add this function at the top level to format the date
private fun formatDate(dateString: String): String {
    return try {
        val date = java.time.LocalDate.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy", Locale.ENGLISH)
        date.format(formatter)
    } catch (e: Exception) {
        dateString // Return original string if parsing fails
    }
} 