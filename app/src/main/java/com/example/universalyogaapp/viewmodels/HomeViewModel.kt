package com.example.universalyogaapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.universalyogaapp.data.Statistics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.universalyogaapp.DatabaseHelper
import android.util.Log
import com.example.universalyogaapp.viewmodels.CourseViewModel
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)
    private val courseViewModel = CourseViewModel(application)
    private val database = FirebaseDatabase.getInstance()
    private val bookingsRef = database.getReference("bookings")
    private val _statistics = MutableStateFlow(Statistics(0, 0, 0, 0))
    val statistics: StateFlow<Statistics> = _statistics

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                // Get booking count from Firebase Realtime Database
                bookingsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val bookingsCount = snapshot.childrenCount.toInt()
                        Log.d("HomeViewModel", "Loaded bookings: $bookingsCount")
                        
                        // Update statistics with the booking count
                        viewModelScope.launch {
                            updateStatisticsWithBookings(bookingsCount)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("HomeViewModel", "Error loading bookings", error.toException())
                        viewModelScope.launch {
                            updateStatisticsWithBookings(0)
                        }
                    }
                })

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error loading statistics", e)
                _statistics.emit(Statistics(0, 0, 0, 0))
            }
        }
    }

    private suspend fun updateStatisticsWithBookings(bookingsCount: Int) {
        try {
            courseViewModel.firebaseCourses.collect { courses ->
                Log.d("HomeViewModel", "Loaded courses: ${courses.size}")
                
                val instructors = dbHelper.getAllInstructors()
                Log.d("HomeViewModel", "Loaded instructors: ${instructors.size}")
                
                val classes = dbHelper.getAllClasses()
                Log.d("HomeViewModel", "Loaded classes: ${classes.size}")

                // Update statistics with actual counts
                _statistics.emit(
                    Statistics(
                        coursesCount = courses.size,
                        instructorsCount = instructors.size,
                        classesCount = classes.size,
                        bookingsCount = bookingsCount
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error updating statistics with bookings", e)
        }
    }

    fun refreshStatistics() {
        loadStatistics()
    }
} 