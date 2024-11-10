package com.example.universalyogaapp.data

import com.google.firebase.database.IgnoreExtraProperties
import androidx.annotation.Keep

@Keep
@IgnoreExtraProperties
data class YogaClass(
    val id: Int = 0,
    val name: String = "",
    val instructorName: String = "",
    val courseId: Long = 0,
    val courseName: String = "",
    val date: String = "",
    val comment: String = ""
) {
    // Required empty constructor for Firebase
    constructor() : this(0, "", "", 0, "", "", "")
} 