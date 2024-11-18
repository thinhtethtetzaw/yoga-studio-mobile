package com.example.universalyogaapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.universalyogaapp.DatabaseHelper
import com.example.universalyogaapp.models.Instructor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue

class InstructorViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)
    private val _instructors = MutableStateFlow<List<Instructor>>(emptyList())
    val instructors: StateFlow<List<Instructor>> = _instructors
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val instructorsRef: DatabaseReference = firebaseDatabase.getReference("instructors")

    init {
        loadLocalInstructors()
    }

    fun loadLocalInstructors() {
        viewModelScope.launch(Dispatchers.IO) {
            val localInstructors = dbHelper.getAllInstructors()
            _instructors.emit(localInstructors)
        }
    }

    // Sync all local changes to Firebase
    fun syncWithFirebase(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Get all local instructors
                val localInstructors = dbHelper.getAllInstructors()
                
                // Clear existing data in Firebase
                instructorsRef.removeValue()
                    .addOnSuccessListener {
                        // Upload all local instructors to Firebase
                        var successCount = 0
                        val totalCount = localInstructors.size
                        
                        if (totalCount == 0) {
                            viewModelScope.launch {
                                withContext(Dispatchers.Main) {
                                    onComplete(true)
                                }
                            }
                            return@addOnSuccessListener
                        }

                        localInstructors.forEach { instructor ->
                            val instructorData = hashMapOf(
                                "id" to instructor.id,
                                "name" to instructor.name,
                                "experience" to instructor.experience,
                                "timestamp" to ServerValue.TIMESTAMP
                            )
                            
                            instructorsRef.child(instructor.id.toString())
                                .setValue(instructorData)
                                .addOnSuccessListener {
                                    successCount++
                                    if (successCount == totalCount) {
                                        viewModelScope.launch {
                                            withContext(Dispatchers.Main) {
                                                onComplete(true)
                                            }
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    viewModelScope.launch {
                                        withContext(Dispatchers.Main) {
                                            onComplete(false)
                                        }
                                    }
                                }
                        }
                    }
                    .addOnFailureListener {
                        viewModelScope.launch {
                            withContext(Dispatchers.Main) {
                                onComplete(false)
                            }
                        }
                    }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }

    fun addInstructor(name: String, experience: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Add to local database
                val localId = dbHelper.insertInstructor(name, experience)
                if (localId != -1L) {
                    // Get the newly created instructor and add it to the current list
                    val currentList = _instructors.value.toMutableList()
                    val newInstructor = Instructor(localId.toInt(), name, experience)
                    currentList.add(newInstructor)
                    
                    // Update the UI immediately
                    _instructors.emit(currentList)
                    
                    withContext(Dispatchers.Main) {
                        onComplete(true)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onComplete(false)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }

    fun updateInstructor(instructor: Instructor) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Update local database
                dbHelper.updateInstructor(instructor)
                // Refresh UI with local data
                loadLocalInstructors()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteInstructor(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Delete from local database
                dbHelper.deleteInstructor(id)
                // Refresh UI with local data
                loadLocalInstructors()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
} 