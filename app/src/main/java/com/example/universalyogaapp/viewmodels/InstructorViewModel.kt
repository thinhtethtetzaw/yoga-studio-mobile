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
        loadInstructors()
    }

    fun loadInstructors() {
        viewModelScope.launch(Dispatchers.IO) {
            val instructorsList = dbHelper.getAllInstructors()
            _instructors.emit(instructorsList)
        }
    }

    fun addInstructor(name: String, experience: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            // First add to SQLite
            val localId = dbHelper.insertInstructor(name, experience)
            
            if (localId != -1L) {
                // Create instructor data for Firebase
                val instructorData = hashMapOf(
                    "id" to localId,
                    "name" to name,
                    "experience" to experience,
                    "timestamp" to ServerValue.TIMESTAMP
                )

                // Add to Firebase
                instructorsRef.child(localId.toString())
                    .setValue(instructorData)
                    .addOnSuccessListener {
                        viewModelScope.launch {
                            loadInstructors()
                            onComplete(true)
                        }
                    }
                    .addOnFailureListener { e ->
                        onComplete(false)
                    }
            } else {
                onComplete(false)
            }
        }
    }

    fun getInstructorById(id: Int): Instructor? {
        return dbHelper.getInstructorById(id)
    }

    fun updateInstructor(instructor: Instructor) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.updateInstructor(instructor)
            
            // Update in Firebase
            val instructorData = hashMapOf(
                "id" to instructor.id,
                "name" to instructor.name,
                "experience" to instructor.experience,
                "lastUpdated" to ServerValue.TIMESTAMP
            )
            
            instructorsRef.child(instructor.id.toString())
                .updateChildren(instructorData as Map<String, Any>)
            
            loadInstructors()
        }
    }

    fun deleteInstructor(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.deleteInstructor(id)
            
            // Delete from Firebase
            instructorsRef.child(id.toString()).removeValue()
            
            loadInstructors()
        }
    }
} 