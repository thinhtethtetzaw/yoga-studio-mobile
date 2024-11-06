package com.example.universalyogaapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.universalyogaapp.models.Instructor
import com.example.universalyogaapp.data.YogaClass
import com.example.universalyogaapp.data.Course

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "UniversalYoga.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        private const val TABLE_INSTRUCTORS = "instructors"
        private const val COLUMN_INSTRUCTOR_ID = "id"
        private const val COLUMN_INSTRUCTOR_NAME = "name"
        private const val COLUMN_INSTRUCTOR_EXPERIENCE = "experience"
        private const val TABLE_CLASSES = "classes"
        private const val KEY_CLASS_ID = "id"
        private const val KEY_CLASS_NAME = "name"
        private const val KEY_INSTRUCTOR_NAME = "instructor_name"
        private const val KEY_COURSE_NAME = "course_name"
        private const val KEY_DATE = "date"
        private const val TABLE_COURSES = "courses"
        private const val COLUMN_COURSE_ID = "id"
        private const val COLUMN_COURSE_NAME = "name"
        private const val COLUMN_COURSE_DESCRIPTION = "description"
        private const val COLUMN_COURSE_DURATION = "duration"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_EMAIL TEXT UNIQUE,
                $COLUMN_PASSWORD TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)

        // Create instructors table
        db.execSQL("""
            CREATE TABLE $TABLE_INSTRUCTORS (
                $COLUMN_INSTRUCTOR_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_INSTRUCTOR_NAME TEXT NOT NULL,
                $COLUMN_INSTRUCTOR_EXPERIENCE TEXT NOT NULL
            )
        """)

        // Add classes table creation
        val createClassesTable = """
            CREATE TABLE $TABLE_CLASSES (
                $KEY_CLASS_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_CLASS_NAME TEXT NOT NULL,
                $KEY_INSTRUCTOR_NAME TEXT NOT NULL,
                $KEY_COURSE_NAME TEXT NOT NULL,
                $KEY_DATE TEXT NOT NULL
            )
        """.trimIndent()
        
        db.execSQL(createClassesTable)

        // Add courses table creation
        val createCoursesTable = """
            CREATE TABLE $TABLE_COURSES (
                $COLUMN_COURSE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_COURSE_NAME TEXT NOT NULL,
                days_of_week TEXT NOT NULL,
                time_of_course TEXT NOT NULL,
                capacity INTEGER NOT NULL,
                duration INTEGER NOT NULL,
                price_per_class REAL NOT NULL,
                type_of_class TEXT NOT NULL,
                description TEXT,
                difficulty_level TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createCoursesTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INSTRUCTORS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CLASSES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_COURSES")
        onCreate(db)
    }

    fun addUser(name: String, email: String, password: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, password)
        }
        return db.insert(TABLE_USERS, null, values)
    }

    fun getUserByEmail(email: String): Pair<String, String>? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_NAME, COLUMN_PASSWORD),
            "$COLUMN_EMAIL = ?",
            arrayOf(email),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
            cursor.close()
            Pair(name, password)
        } else {
            cursor.close()
            null
        }
    }

    fun getUsername(email: String): String? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf("username"),  // Assuming you have a username column
            "email = ?",
            arrayOf(email),
            null,
            null,
            null
        )
        
        return if (cursor.moveToFirst()) {
            cursor.getString(0)
        } else {
            null
        }
    }

    fun insertInstructor(name: String, experience: String): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_INSTRUCTOR_NAME, name)
            put(COLUMN_INSTRUCTOR_EXPERIENCE, experience)
        }
        return db.insert(TABLE_INSTRUCTORS, null, values)
    }

    fun getAllInstructors(): List<Instructor> {
        val instructors = mutableListOf<Instructor>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_INSTRUCTORS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_INSTRUCTOR_NAME ASC"
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(COLUMN_INSTRUCTOR_ID))
                val name = getString(getColumnIndexOrThrow(COLUMN_INSTRUCTOR_NAME))
                val experience = getString(getColumnIndexOrThrow(COLUMN_INSTRUCTOR_EXPERIENCE))
                instructors.add(Instructor(id, name, experience))
            }
        }
        cursor.close()
        return instructors
    }

    fun getInstructorById(id: Int): Instructor? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_INSTRUCTORS,
            arrayOf(COLUMN_INSTRUCTOR_ID, COLUMN_INSTRUCTOR_NAME, COLUMN_INSTRUCTOR_EXPERIENCE),
            "$COLUMN_INSTRUCTOR_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        
        return if (cursor.moveToFirst()) {
            val instructor = Instructor(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_INSTRUCTOR_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INSTRUCTOR_NAME)),
                experience = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INSTRUCTOR_EXPERIENCE))
            )
            cursor.close()
            instructor
        } else {
            cursor.close()
            null
        }
    }

    fun updateInstructor(instructor: Instructor) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_INSTRUCTOR_NAME, instructor.name)
            put(COLUMN_INSTRUCTOR_EXPERIENCE, instructor.experience)
        }
        
        db.update(
            TABLE_INSTRUCTORS,
            values,
            "$COLUMN_INSTRUCTOR_ID = ?",
            arrayOf(instructor.id.toString())
        )
    }

    fun deleteInstructor(id: Int) {
        val db = this.writableDatabase
        db.delete(
            TABLE_INSTRUCTORS,
            "$COLUMN_INSTRUCTOR_ID = ?",
            arrayOf(id.toString())
        )
    }

    fun getAllClasses(): List<YogaClass> {
        val classes = mutableListOf<YogaClass>()
        val db = this.readableDatabase
        
        try {
            val cursor = db.rawQuery("""
                SELECT * FROM $TABLE_CLASSES 
                ORDER BY $KEY_DATE ASC, $KEY_CLASS_NAME ASC
            """.trimIndent(), null)

            cursor.use { c ->
                val idIndex = c.getColumnIndex(KEY_CLASS_ID)
                val nameIndex = c.getColumnIndex(KEY_CLASS_NAME)
                val instructorIndex = c.getColumnIndex(KEY_INSTRUCTOR_NAME)
                val courseIndex = c.getColumnIndex(KEY_COURSE_NAME)
                val dateIndex = c.getColumnIndex(KEY_DATE)

                while (c.moveToNext()) {
                    classes.add(
                        YogaClass(
                            id = c.getInt(idIndex),
                            name = c.getString(nameIndex),
                            instructorName = c.getString(instructorIndex),
                            courseName = c.getString(courseIndex),
                            date = c.getString(dateIndex)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return classes
    }

    fun addClass(
        name: String,
        instructorName: String,
        courseName: String,
        date: String
    ): Long {
        val db = this.writableDatabase
        return try {
            val values = ContentValues().apply {
                put(KEY_CLASS_NAME, name)
                put(KEY_INSTRUCTOR_NAME, instructorName)
                put(KEY_COURSE_NAME, courseName)
                put(KEY_DATE, date)
            }
            
            // For debugging
            println("Inserting class with values: $values")
            
            db.insertOrThrow(TABLE_CLASSES, null, values).also { id ->
                println("Class inserted with ID: $id")
            }
        } catch (e: Exception) {
            println("Error inserting class: ${e.message}")
            e.printStackTrace()
            -1
        }
    }

    fun getAllCourses(): List<Course> {
        val db = this.readableDatabase
        val courses = mutableListOf<Course>()
        
        val cursor = db.query(
            TABLE_COURSES,
            null,
            null,
            null,
            null,
            null,
            null
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(COLUMN_COURSE_ID))
                val courseName = getString(getColumnIndexOrThrow(COLUMN_COURSE_NAME))
                val daysOfWeek = getString(getColumnIndexOrThrow("days_of_week"))
                val timeOfCourse = getString(getColumnIndexOrThrow("time_of_course"))
                val capacity = getInt(getColumnIndexOrThrow("capacity"))
                val duration = getInt(getColumnIndexOrThrow("duration"))
                val pricePerClass = getDouble(getColumnIndexOrThrow("price_per_class"))
                val typeOfClass = getString(getColumnIndexOrThrow("type_of_class"))
                val description = getString(getColumnIndexOrThrow("description"))
                val difficultyLevel = getString(getColumnIndexOrThrow("difficulty_level"))
                
                courses.add(Course(
                    id = id,
                    courseName = courseName,
                    daysOfWeek = daysOfWeek,
                    timeOfCourse = timeOfCourse,
                    capacity = capacity,
                    duration = duration,
                    pricePerClass = pricePerClass,
                    typeOfClass = typeOfClass,
                    description = description,
                    difficultyLevel = difficultyLevel
                ))
            }
        }
        cursor.close()
        
        return courses
    }

    fun getClassCountForCourse(courseName: String): Int {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_CLASSES,
            arrayOf("COUNT(*)"),
            "$KEY_COURSE_NAME = ?",
            arrayOf(courseName),
            null,
            null,
            null
        )
        
        return if (cursor.moveToFirst()) {
            val count = cursor.getInt(0)
            cursor.close()
            count
        } else {
            cursor.close()
            0
        }
    }
}
