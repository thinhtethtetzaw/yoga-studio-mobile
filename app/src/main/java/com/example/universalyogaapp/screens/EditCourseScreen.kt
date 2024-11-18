package com.example.universalyogaapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.universalyogaapp.components.CommonScaffold
import com.example.universalyogaapp.data.Course
import com.example.universalyogaapp.viewmodels.CourseViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun EditCourseScreen(
    navController: NavController,
    courseId: Long,
    courseViewModel: CourseViewModel = viewModel()
) {
    // Load the course when the screen is created
    LaunchedEffect(courseId) {
        courseViewModel.loadCourseById(courseId)
    }

    val selectedCourse by courseViewModel.selectedCourse.collectAsState()

    when {
        selectedCourse == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        else -> {
            // Your existing edit course form UI here
            EditCourseContent(
                course = selectedCourse!!,
                onUpdateCourse = { updatedCourse ->
                    courseViewModel.updateCourse(updatedCourse)
                    navController.navigateUp()
                },
                navController = navController
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCourseContent(
    course: Course,
    onUpdateCourse: (Course) -> Unit,
    navController: NavController
) {
    var courseName by remember { mutableStateOf(course.courseName) }
    var description by remember { mutableStateOf(course.description ?: "") }
    var daysOfWeek by remember { mutableStateOf(course.daysOfWeek) }
    var timeOfCourse by remember { mutableStateOf(course.timeOfCourse) }
    var duration by remember { mutableStateOf(course.duration.toString()) }
    var capacity by remember { mutableStateOf(course.capacity.toString()) }
    var pricePerClass by remember { mutableStateOf(course.pricePerClass.toString()) }
    var difficultyLevel by remember { mutableStateOf(course.difficultyLevel) }
    var typeOfClass by remember { mutableStateOf(course.typeOfClass) }

    CommonScaffold(
        navController = navController,
        title = "Edit Course",
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Wrap the form fields in a scrollable column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = courseName,
                        onValueChange = { courseName = it },
                        label = { Text("Course Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = daysOfWeek,
                        onValueChange = { daysOfWeek = it },
                        label = { Text("Days of Week (comma-separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = timeOfCourse,
                        onValueChange = { timeOfCourse = it },
                        label = { Text("Time of Course") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duration (minutes)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = capacity,
                        onValueChange = { capacity = it },
                        label = { Text("Capacity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = pricePerClass,
                        onValueChange = { pricePerClass = it },
                        label = { Text("Price per Class") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = difficultyLevel,
                        onValueChange = { difficultyLevel = it },
                        label = { Text("Difficulty Level") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = typeOfClass,
                        onValueChange = { typeOfClass = it },
                        label = { Text("Type of Class") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Put the button outside the scroll area, at the bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            val updatedCourse = course.copy(
                                courseName = courseName,
                                description = description,
                                daysOfWeek = daysOfWeek,
                                timeOfCourse = timeOfCourse,
                                duration = duration.toIntOrNull() ?: course.duration,
                                capacity = capacity.toIntOrNull() ?: course.capacity,
                                pricePerClass = pricePerClass.toDoubleOrNull() ?: course.pricePerClass,
                                difficultyLevel = difficultyLevel,
                                typeOfClass = typeOfClass
                            )
                            onUpdateCourse(updatedCourse)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4A635D)
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Update Course")
                    }
                }
            }
        }
    )
} 