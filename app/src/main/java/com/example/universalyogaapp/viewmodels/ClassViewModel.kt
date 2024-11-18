package com.example.universalyogaapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.universalyogaapp.DatabaseHelper
import com.example.universalyogaapp.data.YogaClass
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.android.gms.tasks.Tasks

class ClassViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)
    private val _classes = MutableStateFlow<List<YogaClass>>(emptyList())
    val classes: StateFlow<List<YogaClass>> = _classes
    private val _localOnlyClasses = MutableStateFlow<List<YogaClass>>(emptyList())

    private val _instructors = MutableStateFlow<List<String>>(emptyList())
    val instructors: StateFlow<List<String>> = _instructors

    // Add a set to track deleted class IDs that need to be synced
    private val _deletedClassIds = MutableStateFlow<Set<Int>>(emptySet())

    init {
        loadClasses()
        loadInstructors()
    }

    fun loadClasses() {
        viewModelScope.launch {
            try {
                // Load only from local database
                val localClasses = dbHelper.getAllClasses()
                _classes.emit(localClasses)
                Log.d("ClassViewModel", "Loaded ${localClasses.size} classes from local DB")
            } catch (e: Exception) {
                Log.e("ClassViewModel", "Error loading classes from local DB", e)
            }
        }
    }

    fun addClass(
        name: String,
        instructorName: String,
        courseId: Long,
        courseName: String,
        date: String,
        comment: String = ""
    ) {
        viewModelScope.launch {
            try {
                Log.d("ClassViewModel", """
                    Adding class to database:
                    name: $name
                    instructorName: $instructorName
                    courseId: $courseId
                    courseName: $courseName
                    date: $date
                    comment: $comment
                """.trimIndent())

                // Save to local database
                val id = dbHelper.addClass(
                    name = name,
                    instructorName = instructorName,
                    courseId = courseId,
                    courseName = courseName,
                    date = date,
                    comment = comment
                )
                Log.d("ClassViewModel", "Received ID from database: $id")

                if (id != -1L) {
                    Log.d("ClassViewModel", "Successfully added class to database")
                    // Create new class instance
                    val newClass = YogaClass(
                        id = id.toInt(),
                        name = name,
                        instructorName = instructorName,
                        courseId = courseId,
                        courseName = courseName,
                        date = date,
                        comment = comment
                    )
                    
                    // Add to local-only classes
                    val localClasses = _localOnlyClasses.value.toMutableList()
                    localClasses.add(newClass)
                    _localOnlyClasses.emit(localClasses)
                    
                    // Reload classes
                    loadClasses()
                } else {
                    Log.e("ClassViewModel", "Failed to add class - database returned -1")
                }
            } catch (e: Exception) {
                Log.e("ClassViewModel", "Error adding class", e)
                throw e  // Rethrow to propagate to UI
            }
        }
    }

    fun deleteClass(id: Int) {
        viewModelScope.launch {
            try {
                // Delete from local DB
                dbHelper.deleteClass(id)
                
                // Add to deleted IDs set for sync
                val currentDeletedIds = _deletedClassIds.value.toMutableSet()
                currentDeletedIds.add(id)
                _deletedClassIds.emit(currentDeletedIds)
                
                // Reload from local DB
                loadClasses()
                
                Log.d("ClassViewModel", "Class $id marked for deletion and sync")
            } catch (e: Exception) {
                Log.e("ClassViewModel", "Error deleting class", e)
            }
        }
    }

    fun updateClass(
        id: Int,
        name: String,
        instructorName: String,
        courseId: Long,
        courseName: String,
        date: String,
        comment: String
    ) {
        viewModelScope.launch {
            try {
                // Update only local DB
                dbHelper.updateClass(id, name, instructorName, courseName, date, comment)
                
                // Mark this update for sync
                val updatedClass = YogaClass(
                    id = id,
                    name = name,
                    instructorName = instructorName,
                    courseId = courseId,
                    courseName = courseName,
                    date = date,
                    comment = comment
                )
                
                val localClasses = _localOnlyClasses.value.toMutableList()
                localClasses.add(updatedClass)
                _localOnlyClasses.emit(localClasses)
                
                // Reload from local DB
                loadClasses()
            } catch (e: Exception) {
                Log.e("ClassViewModel", "Error updating class", e)
            }
        }
    }

    suspend fun syncClasses() {
        try {
            val firebaseRef = FirebaseDatabase.getInstance().getReference("classes")
            
            // First, handle deletions
            val deletedIds = _deletedClassIds.value
            Log.d("ClassViewModel", "Syncing deletions: ${deletedIds.size} classes to delete")
            
            deletedIds.forEach { id ->
                try {
                    // Remove from Firebase
                    val deleteRef = firebaseRef.child(id.toString())
                    Tasks.await(deleteRef.removeValue())
                    Log.d("ClassViewModel", "Successfully deleted class $id from Firebase")
                } catch (e: Exception) {
                    Log.e("ClassViewModel", "Failed to delete class $id from Firebase", e)
                }
            }
            
            // Clear deleted IDs after successful sync
            _deletedClassIds.emit(emptySet())

            // Then sync remaining local classes
            val localClasses = dbHelper.getAllClasses()
            Log.d("ClassViewModel", "Syncing ${localClasses.size} local classes to Firebase")

            // Upload local classes to Firebase
            localClasses.forEach { yogaClass ->
                try {
                    val classRef = firebaseRef.child(yogaClass.id.toString())
                    Tasks.await(classRef.setValue(yogaClass.toMap()))
                    Log.d("ClassViewModel", "Successfully synced class ${yogaClass.id} to Firebase")
                } catch (e: Exception) {
                    Log.e("ClassViewModel", "Failed to sync class ${yogaClass.id}", e)
                }
            }

            // Clear local-only changes after successful sync
            _localOnlyClasses.emit(emptyList())
            
            Log.d("ClassViewModel", "Sync completed successfully")
        } catch (e: Exception) {
            Log.e("ClassViewModel", "Error during sync operation", e)
            throw e
        }
    }

    fun loadInstructors() {
        val database = FirebaseDatabase.getInstance()
        database.getReference("instructors").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val instructorsList = mutableListOf<String>()
                snapshot.children.forEach { instructorSnapshot ->
                    val name = instructorSnapshot.child("name").getValue(String::class.java)
                    name?.let { instructorsList.add(it) }
                }
                _instructors.value = instructorsList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ClassViewModel", "Error loading instructors", error.toException())
            }
        })
    }
} 