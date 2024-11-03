package com.example.universalyogaapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val courseName: String,
    val daysOfWeek: String,
    val timeOfCourse: String,
    val capacity: Int,
    val duration: Int,
    val pricePerClass: Double,
    val typeOfClass: String,
    val description: String,
    val difficultyLevel: String
)

enum class YogaType {
    FLOW_YOGA,
    AERIAL_YOGA,
    FAMILY_YOGA
} 