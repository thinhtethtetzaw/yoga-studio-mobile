package com.example.universalyogaapp.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.universalyogaapp.data.Statistics

class HomeViewModel : ViewModel() {
    private val _statistics = MutableStateFlow(Statistics(0, 0, 0, 0))
    val statistics: StateFlow<Statistics> = _statistics

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        val database = FirebaseDatabase.getInstance()
        
        // Load bookings count
        database.getReference("bookings").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalBookings = 0
                snapshot.children.forEach { userSnapshot ->
                    totalBookings += userSnapshot.childrenCount.toInt()
                }
                updateStatistics { it.copy(bookingsCount = totalBookings) }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        // Load courses count
        database.getReference("courses").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val coursesCount = snapshot.childrenCount.toInt()
                updateStatistics { it.copy(coursesCount = coursesCount) }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        // Load instructors count
        database.getReference("instructors").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val instructorsCount = snapshot.childrenCount.toInt()
                updateStatistics { it.copy(instructorsCount = instructorsCount) }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        // Load classes count - Updated to count actual class entries
        database.getReference("classes").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var totalClasses = 0
                snapshot.children.forEach { classSnapshot ->
                    // Each direct child under "classes" is a class entry
                    totalClasses++
                }
                updateStatistics { it.copy(classesCount = totalClasses) }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun updateStatistics(update: (Statistics) -> Statistics) {
        _statistics.value = update(_statistics.value)
    }
} 