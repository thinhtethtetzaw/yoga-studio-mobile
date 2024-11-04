package com.example.universalyogaapp.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
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
    val course by courseViewModel.getCourseById(courseId).collectAsState(initial = null)

    // Show loading state while course is null
    if (course == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Only show form when course data is available
    course?.let { loadedCourse ->
        CreateCourseFormContent(
            navController = navController,
            courseViewModel = courseViewModel,
            isEditing = true,
            initialCourseName = loadedCourse.courseName,
            initialSelectedDays = loadedCourse.daysOfWeek.split(",").toSet(),
            initialFromTime = loadedCourse.timeOfCourse.split(" - ")[0],
            initialToTime = loadedCourse.timeOfCourse.split(" - ")[1],
            initialCapacity = loadedCourse.capacity.toString(),
            initialLevel = loadedCourse.difficultyLevel,
            initialType = loadedCourse.typeOfClass,
            initialPricePerClass = loadedCourse.pricePerClass.toString(),
            initialDescription = loadedCourse.description ?: "",
            existingCourseId = loadedCourse.id
        )
    }
} 