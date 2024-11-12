package com.example.universalyogaapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StatisticsData(
    val coursesCount: Int = 0,
    val classesCount: Int = 0,
    val instructorsCount: Int = 0,
    val bookingsCount: Int = 0
)

class HomeViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    
    private val _statistics = MutableStateFlow(StatisticsData())
    val statistics = _statistics.asStateFlow()

    init {
        fetchStatistics()
    }

    private fun fetchStatistics() {
        viewModelScope.launch {
            // Fetch courses count
            database.getReference("courses").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount.toInt()
                    _statistics.value = _statistics.value.copy(coursesCount = count)
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            // Fetch classes count
            database.getReference("classes").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount.toInt()
                    _statistics.value = _statistics.value.copy(classesCount = count)
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            // Fetch instructors count
            database.getReference("instructors").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount.toInt()
                    _statistics.value = _statistics.value.copy(instructorsCount = count)
                }
                override fun onCancelled(error: DatabaseError) {}
            })

            // Fetch bookings count
            database.getReference("bookings").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.childrenCount.toInt()
                    _statistics.value = _statistics.value.copy(bookingsCount = count)
                }
                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }
} 