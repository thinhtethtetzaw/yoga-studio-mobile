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
                val classesList = withContext(Dispatchers.IO) {
                    dbHelper.getAllClasses()
                }
                _classes.emit(classesList)
                println("Loaded ${classesList.size} classes")
            } catch (e: Exception) {
                println("Error loading classes: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun addClass(
        name: String,
        instructorName: String,
        courseName: String,
        date: String,
        comment: String = ""
    ) {
        viewModelScope.launch {
            try {
                println("Adding class: $name, $instructorName, $courseName, $date, $comment")
                withContext(Dispatchers.IO) {
                    val id = dbHelper.addClass(name, instructorName, courseName, date, comment)
                    println("Class added with id: $id")
                    if (id != -1L) {
                        loadClasses()
                    } else {
                        println("Failed to add class")
                    }
                }
            } catch (e: Exception) {
                println("Error adding class: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun deleteClass(id: Int) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    dbHelper.deleteClass(id)
                }
                loadClasses() // Reload classes after deletion
            } catch (e: Exception) {
                println("Error deleting class: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun updateClass(
        id: Int,
        name: String,
        instructorName: String,
        courseName: String,
        date: String,
        comment: String
    ) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    dbHelper.updateClass(id, name, instructorName, courseName, date, comment)
                }
                loadClasses() // Reload classes after update
            } catch (e: Exception) {
                println("Error updating class: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun loadInstructors() {
        viewModelScope.launch {
            _instructors.value = dbHelper.getAllInstructorNames()
        }
    }
} 