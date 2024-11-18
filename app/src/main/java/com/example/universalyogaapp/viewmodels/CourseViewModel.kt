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
import com.google.android.gms.tasks.Tasks

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
    private val _deletedCourseIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _localChanges = MutableStateFlow<Boolean>(false)
    val localChanges: StateFlow<Boolean> = _localChanges

    init {
        val courseDao = YogaDatabase.getDatabase(application).courseDao()
        repository = CourseRepository(courseDao)
        loadCourses()
    }

    fun insertCourse(course: Course) {
        viewModelScope.launch {
            try {
                // Insert into local database and get the new ID
                val id = repository.insertCourse(course)
                val courseWithId = course.copy(id = id)
                
                Log.d("CourseViewModel", "Course inserted locally with ID: $id")
                
                // Add to local-only courses for later sync
                val localCourses = _localOnlyCourses.value.toMutableList()
                localCourses.add(courseWithId)
                _localOnlyCourses.emit(localCourses)
                
                // Mark that we have local changes
                _localChanges.emit(true)
                
                // Update the courses list for UI immediately
                val currentCourses = _firebaseCourses.value.toMutableList()
                currentCourses.add(courseWithId)
                _firebaseCourses.emit(currentCourses)
                
                // Reload courses to update UI
                loadCourses()
                
                Log.d("CourseViewModel", "Course saved locally and marked for sync. LocalOnlyCourses size: ${localCourses.size}")
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error inserting course", e)
                throw e
            }
        }
    }

    fun getCourseById(id: Long): Flow<Course?> {
        return repository.getCourseById(id)
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            try {
                // Delete from local DB
                repository.deleteCourse(course)
                
                // Add to deleted IDs set for sync
                val currentDeletedIds = _deletedCourseIds.value.toMutableSet()
                currentDeletedIds.add(course.id)
                _deletedCourseIds.emit(currentDeletedIds)
                
                
               loadCourses()
                
                Log.d("CourseViewModel", "Course ${course.id} deleted locally and marked for sync. DeletedIds: ${_deletedCourseIds.value}")
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error deleting course", e)
            }
        }
    }

    fun updateCourse(course: Course) {
        viewModelScope.launch {
            try {
                // Update local DB only
                repository.updateCourse(course)
                
                // Add to local-only courses for sync
                val localCourses = _localOnlyCourses.value.toMutableList()
                localCourses.add(course)
                _localOnlyCourses.emit(localCourses)
                
                // Mark that we have local changes
                _localChanges.emit(true)
                
                // Update the selected course
                _selectedCourse.emit(course)
                
                // Update the courses list for UI
                val currentCourses = _firebaseCourses.value.toMutableList()
                val index = currentCourses.indexOfFirst { it.id == course.id }
                if (index != -1) {
                    currentCourses[index] = course
                } else {
                    currentCourses.add(course)
                }
                _firebaseCourses.emit(currentCourses)
                
                Log.d("CourseViewModel", "Course ${course.id} updated locally")
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error updating course", e)
            }
        }
    }

    private fun addCourseToFirebase(course: Course, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val firebaseRef = FirebaseDatabase.getInstance().getReference("courses")
                val courseRef = firebaseRef.child(course.id.toString())
                
                // Use the complete toMap() function that includes all fields
                val courseMap = course.toMap()
                
                Tasks.await(courseRef.setValue(courseMap))
                Log.d("CourseViewModel", "Successfully added course to Firebase with data: $courseMap")
                onComplete(true)
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error adding course to Firebase", e)
                onComplete(false)
            }
        }
    }

    fun updateCourseInFirebase(courseId: String, course: Course) {
        dbHelper.updateCourseInFirebase(courseId, course) { success ->
            if (success) {
                Log.d("CourseViewModel", "Course successfully updated in Firebase")
                loadCourses()
            } else {
                Log.e("CourseViewModel", "Failed to update course in Firebase")
            }
        }
    }

    fun deleteCourseFromFirebase(courseId: String) {
        dbHelper.deleteCourseFromFirebase(courseId) { success ->
            if (success) {
                Log.d("CourseViewModel", "Course successfully deleted from Firebase")
                loadCourses()
            } else {
                Log.e("CourseViewModel", "Failed to delete course from Firebase")
            }
        }
    }

    fun loadCourses() {
        viewModelScope.launch {
            try {
                // Get courses from local database
                repository.getAllCourses().collect { localCourses ->
                    // Convert courses to CourseWithClassCount
                    val coursesWithCount = localCourses.map { course ->
                        val classCount = dbHelper.getClassCountForCourse(course.courseName)
                        CourseWithClassCount(course, classCount)
                    }
                    _coursesWithCount.emit(coursesWithCount)
                    
                    // Also update firebaseCourses for other parts of the app
                    _firebaseCourses.emit(localCourses)
                    
                    Log.d("CourseViewModel", "Loaded ${localCourses.size} courses from local database")
                }
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error loading courses from local database: ${e.message}")
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
        if (!isOnline()) {
            Log.d("CourseViewModel", "No internet connection, skipping sync")
            return
        }

        try {
            val firebaseRef = FirebaseDatabase.getInstance().getReference("courses")
            
            // Handle deletions first
            val deletedIds = _deletedCourseIds.value
            Log.d("CourseViewModel", "Syncing deletions: ${deletedIds.size} courses to delete")
            
            deletedIds.forEach { id ->
                try {
                    val deleteRef = firebaseRef.child(id.toString())
                    Tasks.await(deleteRef.removeValue())
                    Log.d("CourseViewModel", "Successfully deleted course $id from Firebase")
                } catch (e: Exception) {
                    Log.e("CourseViewModel", "Failed to delete course $id from Firebase", e)
                }
            }
            
            // Clear deleted IDs after successful sync
            _deletedCourseIds.emit(emptySet())

            // Get all courses that need to be synced (both new and updated courses)
            val coursesToSync = _localOnlyCourses.value
            Log.d("CourseViewModel", "Found ${coursesToSync.size} courses to sync")

            // Also get all local courses to ensure we don't miss any
            val allLocalCourses = repository.getAllCourses().first()
            val combinedCourses = (coursesToSync + allLocalCourses).distinctBy { it.id }
            
            Log.d("CourseViewModel", "Syncing total of ${combinedCourses.size} courses to Firebase")

            combinedCourses.forEach { course ->
                try {
                    val courseRef = firebaseRef.child(course.id.toString())
                    val courseMap = course.toMap()
                    Tasks.await(courseRef.setValue(courseMap))
                    Log.d("CourseViewModel", "Successfully synced course ${course.id} to Firebase with data: $courseMap")
                } catch (e: Exception) {
                    Log.e("CourseViewModel", "Failed to sync course ${course.id}", e)
                    // Continue with other courses even if one fails
                    Log.e("CourseViewModel", "Continuing with remaining courses")
                }
            }

            // Clear local-only changes after successful sync
            _localOnlyCourses.emit(emptyList())
            _localChanges.emit(false)
            
            // Reload courses to update UI
            loadCourses()
            
            Log.d("CourseViewModel", "Sync completed successfully")
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

    fun loadCourseById(courseId: Long) {
        viewModelScope.launch {
            try {
                // Load from local database
                repository.getCourseById(courseId).collect { localCourse ->
                    localCourse?.let {
                        _selectedCourse.emit(it)
                        Log.d("CourseViewModel", "Loaded course $courseId from local database")
                    } ?: run {
                        Log.e("CourseViewModel", "Course $courseId not found in local database")
                        _selectedCourse.emit(null)
                    }
                }
            } catch (e: Exception) {
                Log.e("CourseViewModel", "Error loading course $courseId", e)
                _selectedCourse.emit(null)
            }
        }
    }
} 

