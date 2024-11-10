package com.example.universalyogaapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.universalyogaapp.models.Instructor
import com.example.universalyogaapp.data.YogaClass
import com.example.universalyogaapp.data.Course
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "UniversalYoga.db"
        private const val DATABASE_VERSION = 1
        
        // Users table
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        
        // Instructors table
        private const val TABLE_INSTRUCTORS = "instructors"
        private const val COLUMN_INSTRUCTOR_ID = "id"
        private const val COLUMN_INSTRUCTOR_NAME = "name"
        private const val COLUMN_INSTRUCTOR_EXPERIENCE = "experience"
        
        // Classes table
        private const val TABLE_CLASSES = "classes"
        private const val COLUMN_CLASS_ID = "id"
        private const val COLUMN_CLASS_NAME = "name"
        private const val COLUMN_CLASS_INSTRUCTOR_NAME = "instructor_name"
        private const val COLUMN_CLASS_COURSE_NAME = "course_name"
        private const val COLUMN_CLASS_DATE = "date"
        private const val COLUMN_CLASS_COMMENT = "comment"
        
        // Courses table
        private const val TABLE_COURSES = "courses"
        private const val COLUMN_COURSE_ID = "id"
        private const val COLUMN_COURSE_NAME = "name"
        private const val COLUMN_COURSE_DESCRIPTION = "description"
        private const val COLUMN_COURSE_DURATION = "duration"
    }

    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val coursesRef: DatabaseReference = firebaseDatabase.getReference("courses")
    private val db = FirebaseFirestore.getInstance()
    private val coursesCollection = db.collection("courses")
    private val classesRef: DatabaseReference = firebaseDatabase.getReference("classes")

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
                $COLUMN_CLASS_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CLASS_NAME TEXT NOT NULL,
                $COLUMN_CLASS_INSTRUCTOR_NAME TEXT NOT NULL,
                $COLUMN_CLASS_COURSE_NAME TEXT NOT NULL,
                $COLUMN_CLASS_DATE TEXT NOT NULL,
                $COLUMN_CLASS_COMMENT TEXT
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
                SELECT 
                    $COLUMN_CLASS_ID,
                    $COLUMN_CLASS_NAME,
                    $COLUMN_CLASS_INSTRUCTOR_NAME,
                    $COLUMN_CLASS_COURSE_NAME,
                    $COLUMN_CLASS_DATE,
                    $COLUMN_CLASS_COMMENT
                FROM $TABLE_CLASSES 
                ORDER BY $COLUMN_CLASS_DATE ASC, $COLUMN_CLASS_NAME ASC
            """.trimIndent(), null)

            cursor.use { c ->
                val idIndex = c.getColumnIndex(COLUMN_CLASS_ID)
                val nameIndex = c.getColumnIndex(COLUMN_CLASS_NAME)
                val instructorIndex = c.getColumnIndex(COLUMN_CLASS_INSTRUCTOR_NAME)
                val courseIndex = c.getColumnIndex(COLUMN_CLASS_COURSE_NAME)
                val dateIndex = c.getColumnIndex(COLUMN_CLASS_DATE)
                val commentIndex = c.getColumnIndex(COLUMN_CLASS_COMMENT)

                while (c.moveToNext()) {
                    classes.add(
                        YogaClass(
                            id = c.getInt(idIndex),
                            name = c.getString(nameIndex) ?: "",
                            instructorName = c.getString(instructorIndex) ?: "",
                            courseName = c.getString(courseIndex) ?: "",
                            date = c.getString(dateIndex) ?: "",
                            comment = if (commentIndex != -1 && !c.isNull(commentIndex)) {
                                c.getString(commentIndex) ?: ""
                            } else {
                                ""
                            }
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
        date: String,
        comment: String
    ): Long {
        val db = this.writableDatabase
        return try {
            val values = ContentValues().apply {
                put("name", name)
                put("instructor_name", instructorName)
                put("course_name", courseName)
                put("date", date)
                put("comment", comment)
            }
            
            val id = db.insert("classes", null, values)
            println("Added class with id: $id") // Debug log
            id
        } catch (e: Exception) {
            println("Error in addClass: ${e.message}") // Debug log
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
            "$COLUMN_CLASS_COURSE_NAME = ?",
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

    fun deleteClass(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_CLASSES, "$COLUMN_CLASS_ID = ?", arrayOf(id.toString()))
    }

    fun updateClass(
        id: Int,
        name: String,
        instructorName: String,
        courseName: String,
        date: String,
        comment: String
    ): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CLASS_NAME, name)
            put(COLUMN_CLASS_INSTRUCTOR_NAME, instructorName)
            put(COLUMN_CLASS_COURSE_NAME, courseName)
            put(COLUMN_CLASS_DATE, date)
            put(COLUMN_CLASS_COMMENT, comment)
        }
        return db.update(TABLE_CLASSES, values, "$COLUMN_CLASS_ID = ?", arrayOf(id.toString()))
    }

    fun getAllInstructorNames(): List<String> {
        val instructors = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.query(
            "instructors",
            arrayOf("name"),
            null,
            null,
            null,
            null,
            "name ASC"
        )

        with(cursor) {
            while (moveToNext()) {
                instructors.add(getString(getColumnIndexOrThrow("name")))
            }
        }
        cursor.close()
        return instructors
    }

    fun addCourseToFirebase(course: Course, onComplete: (Boolean) -> Unit) {
        try {
            val courseId = coursesRef.push().key
            if (courseId == null) {
                Log.e("DatabaseHelper", "Failed to generate courseId")
                onComplete(false)
                return
            }

            val courseMap = course.toMap()
            
            coursesRef.child(courseId).setValue(courseMap)
                .addOnSuccessListener {
                    Log.d("DatabaseHelper", "Successfully added course to Firebase with ID: ${course.id}")
                    onComplete(true)
                }
                .addOnFailureListener { e ->
                    Log.e("DatabaseHelper", "Error adding course to Firebase", e)
                    onComplete(false)
                }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Exception while adding course to Firebase", e)
            onComplete(false)
        }
    }

    fun getCoursesFromFirebase(onDataReceived: (List<Course>) -> Unit) {
        coursesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val courses = mutableListOf<Course>()
                    for (courseSnapshot in snapshot.children) {
                        val course = courseSnapshot.getValue(Course::class.java)
                        course?.let { 
                            courses.add(it)
                            Log.d("DatabaseHelper", "Loaded course: ${it.courseName}")
                        }
                    }
                    Log.d("DatabaseHelper", "Total courses loaded: ${courses.size}")
                    onDataReceived(courses)
                } catch (e: Exception) {
                    Log.e("DatabaseHelper", "Error parsing courses from Firebase", e)
                    onDataReceived(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseHelper", "Error reading courses: ${error.message}")
                onDataReceived(emptyList())
            }
        })
    }

    fun updateCourseInFirebase(courseId: String, course: Course, callback: (Boolean) -> Unit) {
        // First find the Firebase key for this course
        coursesRef.orderByChild("id").equalTo(courseId.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Get the Firebase key (first child should be our course)
                        val firebaseKey = snapshot.children.first().key
                        if (firebaseKey != null) {
                            // Update the course using the correct Firebase key
                            coursesRef.child(firebaseKey).setValue(course)
                                .addOnSuccessListener {
                                    Log.d("DatabaseHelper", "Successfully updated course in Firebase")
                                    callback(true)
                                }
                                .addOnFailureListener { e ->
                                    Log.e("DatabaseHelper", "Error updating course", e)
                                    callback(false)
                                }
                        } else {
                            Log.e("DatabaseHelper", "Firebase key is null")
                            callback(false)
                        }
                    } else {
                        Log.e("DatabaseHelper", "Course not found in Firebase")
                        callback(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DatabaseHelper", "Error finding course: ${error.message}")
                    callback(false)
                }
            })
    }

    fun deleteCourseFromFirebase(courseId: String, callback: (Boolean) -> Unit) {
        // First find the Firebase key for this course
        coursesRef.orderByChild("id").equalTo(courseId.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Get the Firebase key (first child should be our course)
                        val firebaseKey = snapshot.children.first().key
                        if (firebaseKey != null) {
                            // Delete the course using the correct Firebase key
                            coursesRef.child(firebaseKey).removeValue()
                                .addOnSuccessListener {
                                    Log.d("DatabaseHelper", "Successfully deleted course from Firebase")
                                    callback(true)
                                }
                                .addOnFailureListener { e ->
                                    Log.e("DatabaseHelper", "Error deleting course", e)
                                    callback(false)
                                }
                        } else {
                            Log.e("DatabaseHelper", "Firebase key is null")
                            callback(false)
                        }
                    } else {
                        Log.e("DatabaseHelper", "Course not found in Firebase")
                        callback(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DatabaseHelper", "Error finding course: ${error.message}")
                    callback(false)
                }
            })
    }

    fun addClassToFirebase(yogaClass: YogaClass, onComplete: (Boolean) -> Unit) {
        try {
            val classId = classesRef.push().key
            if (classId == null) {
                Log.e("DatabaseHelper", "Failed to generate classId")
                onComplete(false)
                return
            }

            val classMap = mapOf(
                "id" to yogaClass.id,
                "name" to yogaClass.name,
                "instructorName" to yogaClass.instructorName,
                "courseId" to yogaClass.courseId,
                "courseName" to yogaClass.courseName,
                "date" to yogaClass.date,
                "comment" to yogaClass.comment
            )

            classesRef.child(classId).setValue(classMap)
                .addOnSuccessListener {
                    Log.d("DatabaseHelper", "Successfully added class to Firebase")
                    onComplete(true)
                }
                .addOnFailureListener { e ->
                    Log.e("DatabaseHelper", "Error adding class to Firebase", e)
                    onComplete(false)
                }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Exception while adding class to Firebase", e)
            onComplete(false)
        }
    }

    fun getClassesFromFirebase(onDataReceived: (List<YogaClass>) -> Unit) {
        Log.d("DatabaseHelper", "Starting to fetch classes from Firebase")
        classesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val classes = mutableListOf<YogaClass>()
                    Log.d("DatabaseHelper", "Number of class entries: ${snapshot.childrenCount}")
                    
                    for (classSnapshot in snapshot.children) {
                        try {
                            // Log the raw data
                            Log.d("DatabaseHelper", "Raw class data: ${classSnapshot.value}")
                            
                            // Extract values manually
                            val id = classSnapshot.child("id").getValue(Int::class.java) ?: 0
                            val name = classSnapshot.child("name").getValue(String::class.java) ?: ""
                            val instructorName = classSnapshot.child("instructorName").getValue(String::class.java) ?: ""
                            val courseId = classSnapshot.child("courseId").getValue(Long::class.java) ?: 0
                            val courseName = classSnapshot.child("courseName").getValue(String::class.java) ?: ""
                            val date = classSnapshot.child("date").getValue(String::class.java) ?: ""
                            val comment = classSnapshot.child("comment").getValue(String::class.java) ?: ""

                            val yogaClass = YogaClass(
                                id = id,
                                name = name,
                                instructorName = instructorName,
                                courseId = courseId,
                                courseName = courseName,
                                date = date,
                                comment = comment
                            )
                            
                            classes.add(yogaClass)
                            Log.d("DatabaseHelper", "Successfully parsed class: ${yogaClass.name}")
                        } catch (e: Exception) {
                            Log.e("DatabaseHelper", "Error parsing individual class", e)
                            e.printStackTrace()
                        }
                    }
                    
                    Log.d("DatabaseHelper", "Total classes parsed: ${classes.size}")
                    onDataReceived(classes)
                } catch (e: Exception) {
                    Log.e("DatabaseHelper", "Error parsing classes from Firebase", e)
                    e.printStackTrace()
                    onDataReceived(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DatabaseHelper", "Error reading classes: ${error.message}")
                onDataReceived(emptyList())
            }
        })
    }

    fun updateClassInFirebase(classId: String, yogaClass: YogaClass, callback: (Boolean) -> Unit) {
        classesRef.orderByChild("id").equalTo(classId.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val firebaseKey = snapshot.children.first().key
                        if (firebaseKey != null) {
                            classesRef.child(firebaseKey).setValue(yogaClass)
                                .addOnSuccessListener {
                                    Log.d("DatabaseHelper", "Successfully updated class in Firebase")
                                    callback(true)
                                }
                                .addOnFailureListener { e ->
                                    Log.e("DatabaseHelper", "Error updating class", e)
                                    callback(false)
                                }
                        } else {
                            Log.e("DatabaseHelper", "Firebase key is null")
                            callback(false)
                        }
                    } else {
                        Log.e("DatabaseHelper", "Class not found in Firebase")
                        callback(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DatabaseHelper", "Error finding class: ${error.message}")
                    callback(false)
                }
            })
    }

    fun deleteClassFromFirebase(classId: String, callback: (Boolean) -> Unit) {
        classesRef.orderByChild("id").equalTo(classId.toDouble())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val firebaseKey = snapshot.children.first().key
                        if (firebaseKey != null) {
                            classesRef.child(firebaseKey).removeValue()
                                .addOnSuccessListener {
                                    Log.d("DatabaseHelper", "Successfully deleted class from Firebase")
                                    callback(true)
                                }
                                .addOnFailureListener { e ->
                                    Log.e("DatabaseHelper", "Error deleting class", e)
                                    callback(false)
                                }
                        } else {
                            Log.e("DatabaseHelper", "Firebase key is null")
                            callback(false)
                        }
                    } else {
                        Log.e("DatabaseHelper", "Class not found in Firebase")
                        callback(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("DatabaseHelper", "Error finding class: ${error.message}")
                    callback(false)
                }
            })
    }
}
