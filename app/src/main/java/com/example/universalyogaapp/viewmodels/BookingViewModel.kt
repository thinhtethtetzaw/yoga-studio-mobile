package com.example.universalyogaapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.universalyogaapp.data.Booking
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BookingViewModel : ViewModel() {
    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadBookingsFromFirebase()
    }

    private fun loadBookingsFromFirebase() {
        val database = FirebaseDatabase.getInstance()
        val bookingsRef = database.getReference("bookings")

        bookingsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val bookingsList = mutableListOf<Booking>()
                    
                    Log.d("BookingViewModel", "Snapshot: ${snapshot.value}")
                    
                    snapshot.children.forEach { userSnapshot ->
                        val userId = userSnapshot.key
                        Log.d("BookingViewModel", "Processing user: $userId")
                        
                        userSnapshot.children.forEach { bookingSnapshot ->
                            val bookingMap = bookingSnapshot.value as? Map<*, *>
                            if (bookingMap != null) {
                                try {
                                    val booking = Booking(
                                        bookingTime = (bookingMap["bookingTime"] as? Long) ?: 0L,
                                        classId = (bookingMap["classId"] as? String) ?: "",
                                        className = (bookingMap["className"] as? String) ?: "",
                                        courseId = (bookingMap["courseId"] as? Long)?.toInt() ?: 0,
                                        courseName = (bookingMap["courseName"] as? String) ?: "",
                                        date = (bookingMap["date"] as? String) ?: "",
                                        instructorName = (bookingMap["instructorName"] as? String) ?: "",
                                        price = (bookingMap["price"] as? Long)?.toInt() ?: 0,
                                        status = (bookingMap["status"] as? String) ?: "",
                                        userEmail = (bookingMap["userEmail"] as? String) ?: "",
                                        userId = (bookingMap["userId"] as? String) ?: "",
                                        userName = (bookingMap["userName"] as? String) ?: ""
                                    )
                                    bookingsList.add(booking)
                                    Log.d("BookingViewModel", "Added booking: $booking")
                                } catch (e: Exception) {
                                    Log.e("BookingViewModel", "Error parsing booking: ${e.message}")
                                    _error.value = "Error parsing booking data: ${e.message}"
                                }
                            }
                        }
                    }
                    
                    Log.d("BookingViewModel", "Total bookings found: ${bookingsList.size}")
                    _bookings.value = bookingsList.sortedByDescending { it.bookingTime }
                    _isLoading.value = false
                    _error.value = null
                } catch (e: Exception) {
                    Log.e("BookingViewModel", "Error processing data: ${e.message}")
                    _error.value = "Error loading bookings: ${e.message}"
                    _isLoading.value = false
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("BookingViewModel", "Database error: ${error.message}")
                _error.value = "Database error: ${error.message}"
                _isLoading.value = false
            }
        })
    }
} 