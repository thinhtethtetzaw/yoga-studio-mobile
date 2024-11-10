package com.example.universalyogaapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val courseName: String = "",
    val daysOfWeek: String = "",
    val timeOfCourse: String = "",
    val capacity: Int = 0,
    val duration: Int = 0,
    val pricePerClass: Double = 0.0,
    val typeOfClass: String = "",
    val description: String = "",
    val difficultyLevel: String = ""
) {
    // Required empty constructor for Firebase
    constructor() : this(0, "", "", "", 0, 0, 0.0, "", "", "")

    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "courseName" to courseName,
            "daysOfWeek" to daysOfWeek,
            "timeOfCourse" to timeOfCourse,
            "capacity" to capacity,
            "duration" to duration,
            "pricePerClass" to pricePerClass,
            "typeOfClass" to typeOfClass,
            "description" to description,
            "difficultyLevel" to difficultyLevel
        )
    }
}

enum class YogaType {
    FLOW_YOGA,
    AERIAL_YOGA,
    FAMILY_YOGA
} 