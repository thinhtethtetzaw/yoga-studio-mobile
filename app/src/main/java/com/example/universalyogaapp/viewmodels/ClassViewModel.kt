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
                println("Loaded ${classesList.size} classes") // Debug log
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addClass(name: String, instructorName: String, courseName: String, date: String) {
        viewModelScope.launch {
            try {
                val id = withContext(Dispatchers.IO) {
                    dbHelper.addClass(name, instructorName, courseName, date)
                }
                if (id != -1L) {
                    println("Class added successfully with id: $id") // Debug log
                    loadClasses() // Reload the classes after successful addition
                } else {
                    println("Failed to add class") // Debug log
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
} 