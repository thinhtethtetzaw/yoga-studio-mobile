package com.example.universalyogaapp

import android.app.Application
import com.example.universalyogaapp.data.YogaDatabase
import com.google.firebase.FirebaseApp

class YogaApplication : Application() {
    val database: YogaDatabase by lazy { 
        YogaDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        database
        FirebaseApp.initializeApp(this)
    }
} 