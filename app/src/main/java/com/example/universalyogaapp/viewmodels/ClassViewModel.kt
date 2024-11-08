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

    init {
        loadClasses()
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
} 