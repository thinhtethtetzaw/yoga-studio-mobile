package com.example.universalyogaapp.viewmodels

import android.app.Application
import android.util.Log
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
    private val _firebaseCourses = MutableStateFlow<List<Course>>(emptyList())
    val firebaseCourses: StateFlow<List<Course>> = _firebaseCourses
    
    init {
        val courseDao = YogaDatabase.getDatabase(application).courseDao()
        repository = CourseRepository(courseDao)
        loadCoursesFromFirebase()
    }

    fun insertCourse(course: Course) {
        viewModelScope.launch {
            // First insert into local database to get the generated ID
            val id = repository.insertCourse(course)
            // Create a new course object with the generated ID
            val courseWithId = course.copy(id = id.toInt())
            // Add to Firebase with the correct ID
            addCourseToFirebase(courseWithId)
        }
    }

    fun getCourseById(id: Long): Flow<Course?> {
        return repository.getCourseById(id)
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            repository.deleteCourse(course)
            // Also delete from Firebase using the course's ID
            deleteCourseFromFirebase(course.id.toString())
        }
    }

    fun updateCourse(course: Course) {
        viewModelScope.launch {
            repository.updateCourse(course)
            // Also update in Firebase using the course's ID
            updateCourseInFirebase(course.id.toString(), course)
        }
    }

    fun addCourseToFirebase(course: Course) {
        dbHelper.addCourseToFirebase(course) { success ->
            if (success) {
                Log.d("CourseViewModel", "Course successfully added to Firebase")
                loadCoursesFromFirebase()
            } else {
                Log.e("CourseViewModel", "Failed to add course to Firebase")
            }
        }
    }

    fun loadCoursesFromFirebase() {
        dbHelper.getCoursesFromFirebase { courses ->
            viewModelScope.launch {
                try {
                    _firebaseCourses.emit(courses)
                    Log.d("CourseViewModel", "Loaded ${courses.size} courses from Firebase")
                } catch (e: Exception) {
                    Log.e("CourseViewModel", "Error loading courses from Firebase", e)
                }
            }
        }
    }

    fun updateCourseInFirebase(courseId: String, course: Course) {
        dbHelper.updateCourseInFirebase(courseId, course) { success ->
            if (success) {
                Log.d("CourseViewModel", "Course successfully updated in Firebase")
                loadCoursesFromFirebase()
            } else {
                Log.e("CourseViewModel", "Failed to update course in Firebase")
            }
        }
    }

    fun deleteCourseFromFirebase(courseId: String) {
        dbHelper.deleteCourseFromFirebase(courseId) { success ->
            if (success) {
                Log.d("CourseViewModel", "Course successfully deleted from Firebase")
                loadCoursesFromFirebase()
            } else {
                Log.e("CourseViewModel", "Failed to delete course from Firebase")
            }
        }
    }
} 

