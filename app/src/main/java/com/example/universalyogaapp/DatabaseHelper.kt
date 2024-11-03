package com.example.universalyogaapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.universalyogaapp.models.Instructor

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
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INSTRUCTORS")
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
}
