package com.example.universalyogaapp.data

data class Booking(
    val bookingTime: Long = 0,
    val classId: String = "",
    val className: String = "",
    val courseId: Int = 0,
    val courseName: String = "",
    val date: String = "",
    val instructorName: String = "",
    val price: Int = 0,
    val status: String = "",
    val userEmail: String = "",
    val userId: String = "",
    val userName: String = ""
)