package com.example.universalyogaapp.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.universalyogaapp.viewmodels.CourseViewModel

@Composable
fun EditCourseScreen(
    navController: NavController,
    courseId: Long,
    courseViewModel: CourseViewModel = viewModel()
) {
    var isLoading by remember { mutableStateOf(true) }
    val course by courseViewModel.selectedCourse.collectAsState()

    // Fetch course data when the screen is first composed
    LaunchedEffect(Unit) {
        isLoading = true
        courseViewModel.fetchCourseByIdFromFirebase(courseId)
    }

    // Update loading state when course changes
    LaunchedEffect(course) {
        if (course != null || course == null) {
            // Set loading to false whether we got a course or not
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (course == null) {
        // Handle the case where no course was found
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Course not found")
        }
    } else {
        // Show the form when course is loaded
        CreateCourseFormContent(
            navController = navController,
            courseViewModel = courseViewModel,
            isEditing = true,
            initialCourseName = course!!.courseName,
            initialSelectedDays = course!!.daysOfWeek.split(",").toSet(),
            initialFromTime = course!!.timeOfCourse.split(" - ")[0],
            initialToTime = course!!.timeOfCourse.split(" - ")[1],
            initialCapacity = course!!.capacity.toString(),
            initialLevel = course!!.difficultyLevel,
            initialType = course!!.typeOfClass,
            initialPricePerClass = course!!.pricePerClass.toString(),
            initialDescription = course!!.description ?: "",
            existingCourseId = course!!.id
        )
    }
} 