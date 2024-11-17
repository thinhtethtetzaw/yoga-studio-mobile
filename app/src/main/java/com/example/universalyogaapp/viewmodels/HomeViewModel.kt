package com.example.universalyogaapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.universalyogaapp.data.Statistics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.google.firebase.firestore.FirebaseFirestore
import com.example.universalyogaapp.DatabaseHelper

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val dbHelper = DatabaseHelper(application)
    private val firestore = FirebaseFirestore.getInstance()
    private val _statistics = MutableStateFlow(Statistics(0, 0, 0, 0))
    val statistics: StateFlow<Statistics> = _statistics

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            try {
                // Get courses count from Firestore
                firestore.collection("courses").get()
                    .addOnSuccessListener { coursesSnapshot ->
                        val coursesCount = coursesSnapshot.size()

                        // Get instructors count
                        val instructors = dbHelper.getAllInstructors()
                        val instructorsCount = instructors.size

                        // Get classes count
                        val classes = dbHelper.getAllClasses()
                        val classesCount = classes.size

                        // Get bookings count (if you have a bookings collection)
                        firestore.collection("bookings").get()
                            .addOnSuccessListener { bookingsSnapshot ->
                                val bookingsCount = bookingsSnapshot.size()

                                // Update statistics with actual counts
                                viewModelScope.launch {
                                    _statistics.emit(
                                        Statistics(
                                            coursesCount = coursesCount,
                                            instructorsCount = instructorsCount,
                                            classesCount = classesCount,
                                            bookingsCount = bookingsCount
                                        )
                                    )
                                }
                            }
                            .addOnFailureListener {
                                // If bookings fetch fails, update with other statistics
                                viewModelScope.launch {
                                    _statistics.emit(
                                        Statistics(
                                            coursesCount = coursesCount,
                                            instructorsCount = instructorsCount,
                                            classesCount = classesCount,
                                            bookingsCount = 0
                                        )
                                    )
                                }
                            }
                    }
                    .addOnFailureListener {
                        // If courses fetch fails, emit zeros
                        viewModelScope.launch {
                            _statistics.emit(Statistics(0, 0, 0, 0))
                        }
                    }
            } catch (e: Exception) {
                // If any error occurs, emit zeros
                viewModelScope.launch {
                    _statistics.emit(Statistics(0, 0, 0, 0))
                }
            }
        }
    }

    fun refreshStatistics() {
        loadStatistics()
    }
} 