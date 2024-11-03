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

class InstructorViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)
    private val _instructors = MutableStateFlow<List<Instructor>>(emptyList())
    val instructors: StateFlow<List<Instructor>> = _instructors

    init {
        loadInstructors()
    }

    private fun loadInstructors() {
        viewModelScope.launch(Dispatchers.IO) {
            val instructorsList = dbHelper.getAllInstructors()
            _instructors.value = instructorsList
        }
    }

    fun addInstructor(name: String, experience: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = dbHelper.insertInstructor(name, experience)
            if (id != -1L) {
                loadInstructors() // Reload the list after successful insertion
            }
        }
    }
} 