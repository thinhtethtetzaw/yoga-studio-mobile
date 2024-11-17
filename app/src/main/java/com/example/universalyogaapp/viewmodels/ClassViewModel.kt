package com.example.universalyogaapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.universalyogaapp.DatabaseHelper
import com.example.universalyogaapp.data.YogaClass
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ClassViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)
    private val _classes = MutableStateFlow<List<YogaClass>>(emptyList())
    val classes: StateFlow<List<YogaClass>> = _classes

    private val _instructors = MutableStateFlow<List<String>>(emptyList())
    val instructors: StateFlow<List<String>> = _instructors

    init {
        loadClasses()
        loadInstructors()
    }

    fun loadClasses() {
        viewModelScope.launch {
            try {
                Log.d("ClassViewModel", "Starting to load classes")
                dbHelper.getClassesFromFirebase { classesList ->
                    viewModelScope.launch {
                        try {
                            Log.d("ClassViewModel", "Received ${classesList.size} classes")
                            classesList.forEach { yogaClass ->
                                Log.d("ClassViewModel", "Class: ${yogaClass.name}")
                            }
                            _classes.emit(classesList)
                        } catch (e: Exception) {
                            Log.e("ClassViewModel", "Error emitting classes", e)
                            e.printStackTrace()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ClassViewModel", "Error loading classes", e)
                e.printStackTrace()
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
                Log.d("ClassViewModel", "Adding class: $name, $instructorName, $courseName")
                val yogaClass = YogaClass(
                    id = System.currentTimeMillis().toInt(),
                    name = name,
                    instructorName = instructorName,
                    courseId = courseId,
                    courseName = courseName,
                    date = date,
                    comment = comment
                )
                
                dbHelper.addClassToFirebase(yogaClass) { success ->
                    viewModelScope.launch {
                        if (success) {
                            Log.d("ClassViewModel", "Class added successfully")
                            loadClasses()
                        } else {
                            Log.e("ClassViewModel", "Failed to add class")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ClassViewModel", "Error adding class", e)
                e.printStackTrace()
            }
        }
    }

    fun deleteClass(id: Int) {
        viewModelScope.launch {
            try {
                dbHelper.deleteClassFromFirebase(id.toString()) { success ->
                    viewModelScope.launch {
                        if (success) {
                            println("Class deleted from Firebase successfully")
                            loadClasses()
                        } else {
                            println("Failed to delete class from Firebase")
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error deleting class from Firebase: ${e.message}")
                e.printStackTrace()
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
                val updatedClass = YogaClass(
                    id = id,
                    name = name,
                    instructorName = instructorName,
                    courseId = courseId,
                    courseName = courseName,
                    date = date,
                    comment = comment
                )
                
                dbHelper.updateClassInFirebase(id.toString(), updatedClass) { success ->
                    viewModelScope.launch {
                        if (success) {
                            println("Class updated in Firebase successfully")
                            loadClasses()
                        } else {
                            println("Failed to update class in Firebase")
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error updating class in Firebase: ${e.message}")
                e.printStackTrace()
            }
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
                // Handle error
            }
        })
    }
} 