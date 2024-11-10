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
        loadCoursesWithCount()
        loadCoursesFromFirebase()
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
        }
    }

    fun updateCourse(course: Course) {
        viewModelScope.launch {
            repository.updateCourse(course)
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
                Log.d("CourseViewModel", "Loaded ${courses.size} courses from Firebase")
                _firebaseCourses.emit(courses)
            }
        }
    }

    fun updateCourseInFirebase(courseId: String, course: Course) {
        dbHelper.updateCourseInFirebase(courseId, course) { success ->
            if (success) {
                // Optionally handle successful update
                loadCoursesFromFirebase()
            }
        }
    }

    fun deleteCourseFromFirebase(courseId: String) {
        dbHelper.deleteCourseFromFirebase(courseId) { success ->
            if (success) {
                // Optionally handle successful deletion
                loadCoursesFromFirebase()
            }
        }
    }
} 

