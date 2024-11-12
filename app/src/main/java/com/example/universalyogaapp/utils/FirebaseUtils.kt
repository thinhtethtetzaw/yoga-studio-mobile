package com.example.universalyogaapp.utils

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.example.universalyogaapp.models.Admin
import kotlinx.coroutines.tasks.await
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

object FirebaseUtils {
    private val database = Firebase.database
    private val adminsRef = database.getReference("admins")

    suspend fun syncAdmins(localAdmins: List<Admin>): Result<Unit> {
        return try {
            // Get all admins from Firebase
            val snapshot = adminsRef.get().await()
            val firebaseAdmins = snapshot.children.mapNotNull { it.getValue(Admin::class.java) }
            
            // Find admins that are in local but not in Firebase
            localAdmins.forEach { localAdmin ->
                if (!firebaseAdmins.any { it.email == localAdmin.email }) {
                    // Add to Firebase
                    val newKey = adminsRef.push().key ?: return@forEach
                    adminsRef.child(newKey).setValue(localAdmin).await()
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun addAdmin(admin: Admin, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val newKey = adminsRef.push().key ?: return
        adminsRef.child(newKey).setValue(admin)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun listenForAdminChanges(onDataChange: (List<Admin>) -> Unit) {
        adminsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val admins = snapshot.children.mapNotNull { 
                    it.getValue(Admin::class.java) 
                }
                onDataChange(admins)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
} 