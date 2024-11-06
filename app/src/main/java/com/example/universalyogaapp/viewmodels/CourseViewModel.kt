package com.example.universalyogaapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.universalyogaapp.data.Course
import com.example.universalyogaapp.data.CourseRepository
import com.example.universalyogaapp.data.YogaDatabase
import com.example.universalyogaapp.DatabaseHelper
import com.example.universalyogaapp.data.CourseWithClassCount
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CourseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CourseRepository
    private val dbHelper = DatabaseHelper(application)
    private val _coursesWithCount = MutableStateFlow<List<CourseWithClassCount>>(emptyList())
    val coursesWithCount: StateFlow<List<CourseWithClassCount>> = _coursesWithCount
    
    init {
        val courseDao = YogaDatabase.getDatabase(application).courseDao()
        repository = CourseRepository(courseDao)
        loadCoursesWithCount()
    }

    private fun loadCoursesWithCount() {
        viewModelScope.launch {
            repository.allCourses.collect { courses ->
                val coursesWithCount = courses.map { course ->
                    CourseWithClassCount(
                        course = course,
                        classCount = dbHelper.getClassCountForCourse(course.courseName)
                    )
                }
                _coursesWithCount.emit(coursesWithCount)
            }
        }
    }

    fun insertCourse(course: Course) {
        viewModelScope.launch {
            repository.insertCourse(course)
        }
    }

    fun getCourseById(id: Long): Flow<Course?> {
        return repository.getCourseById(id)
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            repository.deleteCourse(course)
        }
    }

    fun updateCourse(course: Course) {
        viewModelScope.launch {
            repository.updateCourse(course)
        }
    }
} 