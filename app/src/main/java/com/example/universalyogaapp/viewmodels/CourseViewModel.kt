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
                // Insert into local database only
                val id = repository.insertCourse(course)
                val courseWithId = course.copy(id = id)
                
                // Add to local-only courses for later sync
                val localCourses = _localOnlyCourses.value.toMutableList()
                localCourses.add(courseWithId)
                _localOnlyCourses.emit(localCourses)
                
                // Mark that we have local changes
                _localChanges.emit(true)
                
                // Update the courses list for UI
                val currentCourses = _firebaseCourses.value.toMutableList()
                currentCourses.add(courseWithId)
                _firebaseCourses.emit(currentCourses)
                
                Log.d("CourseViewModel", "Course added locally")
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

    // suspend fun syncClasses() {
    //     try {
    //         val firebaseRef = FirebaseDatabase.getInstance().getReference("classes")
            
    //         // First, handle deletions
    //         val deletedIds = _deletedClassIds.value
    //         Log.d("ClassViewModel", "Syncing deletions: ${deletedIds.size} classes to delete")
            
    //         deletedIds.forEach { id ->
    //             try {
    //                 // Remove from Firebase
    //                 val deleteRef = firebaseRef.child(id.toString())
    //                 Tasks.await(deleteRef.removeValue())
    //                 Log.d("ClassViewModel", "Successfully deleted class $id from Firebase")
    //             } catch (e: Exception) {
    //                 Log.e("ClassViewModel", "Failed to delete class $id from Firebase", e)
    //             }
    //         }
            
    //         // Clear deleted IDs after successful sync
    //         _deletedClassIds.emit(emptySet())

    //         // Then sync remaining local classes
    //         val localClasses = dbHelper.getAllClasses()
    //         Log.d("ClassViewModel", "Syncing ${localClasses.size} local classes to Firebase")

    //         // Upload local classes to Firebase
    //         localClasses.forEach { yogaClass ->
    //             try {
    //                 val classRef = firebaseRef.child(yogaClass.id.toString())
    //                 Tasks.await(classRef.setValue(yogaClass.toMap()))
    //                 Log.d("ClassViewModel", "Successfully synced class ${yogaClass.id} to Firebase")
    //             } catch (e: Exception) {
    //                 Log.e("ClassViewModel", "Failed to sync class ${yogaClass.id}", e)
    //             }
    //         }

    //         // Clear local-only changes after successful sync
    //         _localOnlyClasses.emit(emptyList())
            
    //         Log.d("ClassViewModel", "Sync completed successfully")
    //     } catch (e: Exception) {
    //         Log.e("ClassViewModel", "Error during sync operation", e)
    //         throw e
    //     }
    // }

    suspend fun syncCourses() {
        try {
            val firebaseRef = FirebaseDatabase.getInstance().getReference("courses")
            
            // First, handle deletions
            val deletedIds = _deletedCourseIds.value
            Log.d("CourseViewModel", "Syncing deletions: ${deletedIds.size} courses to delete")
            
            deletedIds.forEach { id ->
                try {
                    // Remove from Firebase
                    val deleteRef = firebaseRef.child(id.toString())
                    Tasks.await(deleteRef.removeValue())
                    Log.d("CourseViewModel", "Successfully deleted course $id from Firebase")
                } catch (e: Exception) {
                    Log.e("CourseViewModel", "Failed to delete course $id from Firebase", e)
                }
            }
            
            // Clear deleted IDs after successful sync
            _deletedCourseIds.emit(emptySet())

            // Then sync remaining local courses
            val localCourses = repository.getAllCourses().first()
            Log.d("CourseViewModel", "Syncing ${localCourses.size} local courses to Firebase")

            // Upload local courses to Firebase
            localCourses.forEach { course ->
                try {
                    val courseRef = firebaseRef.child(course.id.toString())
                    Tasks.await(courseRef.setValue(course.toMap()))
                    Log.d("CourseViewModel", "Successfully synced course ${course.id} to Firebase")
                } catch (e: Exception) {
                    Log.e("CourseViewModel", "Failed to sync course ${course.id}", e)
                }
            }

            // Clear local-only changes after successful sync
            _localOnlyCourses.emit(emptyList())
            _localChanges.emit(false)
            
            Log.d("CourseViewModel", "Sync completed successfully")
            
            // Reload courses to update UI
            loadCourses()
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

