package com.example.universalyogaapp.data

import kotlinx.coroutines.flow.Flow

class CourseRepository(private val courseDao: CourseDao) {
    val allCourses: Flow<List<Course>> = courseDao.getAllCourses()

    suspend fun insertCourse(course: Course) {
        courseDao.insertCourse(course)
    }

    fun getCourseById(id: Long): Flow<Course?> {
        return courseDao.getCourseById(id)
    }

    suspend fun deleteCourse(course: Course) {
        courseDao.deleteCourse(course)
    }
} 