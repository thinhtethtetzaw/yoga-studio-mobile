package com.example.universalyogaapp.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestore

class CourseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CourseRepository
    private val dbHelper = DatabaseHelper(application)
    private val _coursesWithCount = MutableStateFlow<List<CourseWithClassCount>>(emptyList())
    val coursesWithCount: StateFlow<List<CourseWithClassCount>> = _coursesWithCount
    private val _firebaseCourses = MutableStateFlow<List<Course>>(emptyList())
    val firebaseCourses: StateFlow<List<Course>> = _firebaseCourses
    private val _selectedCourse = MutableStateFlow<Course?>(null)
    val selectedCourse: StateFlow<Course?> = _selectedCourse
    private val firestore = FirebaseFirestore.getInstance()
    private val _localOnlyCourses = MutableStateFlow<List<Course>>(emptyList())
    val localOnlyCourses: StateFlow<List<Course>> = _localOnlyCourses
    
    init {
        val courseDao = YogaDatabase.getDatabase(application).courseDao()
        repository = CourseRepository(courseDao)
        loadCoursesFromFirebase()
    }

    fun insertCourse(course: Course) {
        viewModelScope.launch {
            try {
                // Insert into local database
                val id = repository.insertCourse(course)
                val courseWithId = course.copy(id = id)
                
                // Update local courses list
                val currentCourses = _firebaseCourses.value.toMutableList()
                currentCourses.add(courseWithId)
                _firebaseCourses.emit(currentCourses)
                
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error inserting course", e)
            }
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

    fun addCourseToFirebase(course: Course, onComplete: (Boolean) -> Unit = {}) {
        try {
            val firebaseRef = FirebaseDatabase.getInstance().getReference("courses")
            val courseRef = firebaseRef.child(course.id.toString())
            courseRef.setValue(course.toMap())
                .addOnSuccessListener {
                    Log.d("CourseViewModel", "Successfully added course to Firebase")
                    onComplete(true)
                }
                .addOnFailureListener { e ->
                    Log.e("CourseViewModel", "Error adding course to Firebase", e)
                    onComplete(false)
                }
        } catch (e: Exception) {
            Log.e("CourseViewModel", "Exception while adding course to Firebase", e)
            onComplete(false)
        }
    }

    fun loadCoursesFromFirebase() {
        viewModelScope.launch {
            try {
                val firebaseRef = FirebaseDatabase.getInstance().getReference("courses")
                firebaseRef.get()
                    .addOnSuccessListener { snapshot ->
                        val firebaseCourses = snapshot.children.mapNotNull { 
                            it.getValue(Course::class.java) 
                        }
                        viewModelScope.launch {
                            _firebaseCourses.emit(firebaseCourses)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("CourseViewModel", "Error loading courses from Firebase", e)
                        // If Firebase fails, load from local database
                        viewModelScope.launch {
                            val localCourses = repository.getAllCourses().first()
                            _firebaseCourses.emit(localCourses)
                        }
                    }
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error loading courses", e)
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

    fun loadCourses() {
        viewModelScope.launch {
            try {
                // Get courses from Firebase
                dbHelper.getCoursesFromFirebase { courses ->
                    viewModelScope.launch {
                        // Convert courses to CourseWithClassCount
                        val coursesWithCount = courses.map { course ->
                            val classCount = dbHelper.getClassCountForCourse(course.courseName)
                            CourseWithClassCount(course, classCount)
                        }
                        _coursesWithCount.emit(coursesWithCount)
                    }
                }
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error loading courses: ${e.message}")
            }
        }
    }

    fun fetchCourseByIdFromFirebase(courseId: Long) {
        val dbRef = FirebaseDatabase.getInstance().getReference("courses")
        dbRef.orderByChild("id").equalTo(courseId.toDouble())
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Since we're querying by ID, there should only be one result
                    snapshot.children.firstOrNull()?.let { courseSnapshot ->
                        val course = courseSnapshot.getValue(Course::class.java)
                        viewModelScope.launch {
                            _selectedCourse.emit(course)
                        }
                    }
                } else {
                    viewModelScope.launch {
                        _selectedCourse.emit(null)
                    }
                }
            }
            .addOnFailureListener {
                viewModelScope.launch {
                    _selectedCourse.emit(null)
                }
                Log.e("CourseViewModel", "Error getting course data", it)
            }
    }

    suspend fun syncCourses() {
        try {
            // Get all local courses
            val localCourses = repository.getAllCourses().first()
            
            // Get all Firebase courses
            val firebaseRef = FirebaseDatabase.getInstance().getReference("courses")
            val snapshot = firebaseRef.get().await()
            val firebaseCourses = snapshot.children.mapNotNull { 
                it.getValue(Course::class.java) 
            }
            
            // Create a map of existing Firebase course IDs
            val firebaseCourseIds = firebaseCourses.map { it.id }.toSet()
            
            // Upload local courses that don't exist in Firebase
            localCourses.forEach { localCourse ->
                if (!firebaseCourseIds.contains(localCourse.id)) {
                    try {
                        // Add course to Firebase Realtime Database
                        val courseRef = firebaseRef.child(localCourse.id.toString())
                        courseRef.setValue(localCourse.toMap())
                            .addOnSuccessListener {
                                Log.d("CourseViewModel", "Successfully synced local course ${localCourse.id} to Firebase")
                            }
                            .addOnFailureListener { e ->
                                Log.e("CourseViewModel", "Failed to sync course ${localCourse.id}", e)
                            }
                            .await()
                    } catch (e: Exception) {
                        Log.e("CourseViewModel", "Error syncing course ${localCourse.id}", e)
                    }
                }
            }
            
            // Download Firebase courses that don't exist locally
            val localCourseIds = localCourses.map { it.id }.toSet()
            firebaseCourses.forEach { firebaseCourse ->
                if (!localCourseIds.contains(firebaseCourse.id)) {
                    try {
                        repository.insertCourse(firebaseCourse)
                        Log.d("CourseViewModel", "Successfully synced Firebase course ${firebaseCourse.id} to local DB")
                    } catch (e: Exception) {
                        Log.e("CourseViewModel", "Error syncing Firebase course ${firebaseCourse.id} to local", e)
                    }
                }
            }
            
            // Reload courses to update the UI
            loadCoursesFromFirebase()
            
        } catch (e: Exception) {
            Log.e("CourseViewModel", "Error during sync operation", e)
            throw e
        }
    }

    private fun isOnline(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    suspend fun addCourse(course: Course) {
        // Always save to local database
        insertCourse(course)
        
        // If online, sync with Firebase
        if (isOnline()) {
            try {
                addCourseToFirebase(course)
            } catch (e: Exception) {
                // Handle Firebase sync error
                // Course is still saved locally
                e.printStackTrace()
            }
        }
    }
} 

