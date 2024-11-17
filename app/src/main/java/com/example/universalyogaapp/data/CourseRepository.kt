package com.example.universalyogaapp.data

import kotlinx.coroutines.flow.Flow

class CourseRepository(private val courseDao: CourseDao) {
    fun getAllCourses(): Flow<List<Course>> {
        return courseDao.getAllCourses()
    }

    suspend fun insertCourse(course: Course): Long {
        return courseDao.insertCourse(course)
    }

    fun getCourseById(id: Long): Flow<Course?> {
        return courseDao.getCourseById(id)
    }

    suspend fun deleteCourse(course: Course) {
        courseDao.deleteCourse(course)
    }

    suspend fun updateCourse(course: Course) {
        courseDao.updateCourse(course)
    }
} 