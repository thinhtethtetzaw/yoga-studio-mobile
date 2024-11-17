package com.example.universalyogaapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.universalyogaapp.data.Statistics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.universalyogaapp.DatabaseHelper
import android.util.Log
import com.example.universalyogaapp.viewmodels.CourseViewModel

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)
    private val courseViewModel = CourseViewModel(application)
    private val _statistics = MutableStateFlow(Statistics(0, 0, 0, 0))
    val statistics: StateFlow<Statistics> = _statistics

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                // Collect courses from CourseViewModel
                courseViewModel.firebaseCourses.collect { courses ->
                    Log.d("HomeViewModel", "Loaded courses: ${courses.size}")
                    
                    val instructors = dbHelper.getAllInstructors()
                    Log.d("HomeViewModel", "Loaded instructors: ${instructors.size}")
                    
                    val classes = dbHelper.getAllClasses()
                    Log.d("HomeViewModel", "Loaded classes: ${classes.size}")

                    // Update statistics with actual counts
                    _statistics.emit(
                        Statistics(
                            coursesCount = courses.size,
                            instructorsCount = instructors.size,
                            classesCount = classes.size,
                            bookingsCount = 0 // You can implement this later if needed
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading statistics", e)
                _statistics.emit(Statistics(0, 0, 0, 0))
            }
        }
    }

    fun refreshStatistics() {
        loadStatistics()
    }
} 