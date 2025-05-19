package com.example.studypal.data.repository

import android.app.Activity
import android.app.Application
import android.util.Log
import com.example.studypal.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<FirebaseUser>
    suspend fun signUp(email: String, password: String, activity: Activity): Result<FirebaseUser>
    suspend fun signOut()
    fun getCurrentUser(): FirebaseUser?
    suspend fun getUserData(userId: String): Result<User>
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val application: Application
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            Log.d("AuthRepository", "Attempting to sign in user: $email")
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Log.d("AuthRepository", "Sign in successful for user: ${result.user?.uid}")
            Result.success(result.user!!)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign in failed", e)
            Result.failure(e)
        }
    }

    override suspend fun signUp(email: String, password: String, activity: Activity): Result<FirebaseUser> {
        return try {
            Log.d("AuthRepository", "Attempting to sign up user: $email")
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Log.d("AuthRepository", "Sign up successful for user: ${result.user?.uid}")
            Result.success(result.user!!)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign up failed", e)
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        try {
            Log.d("AuthRepository", "Attempting to sign out user: ${auth.currentUser?.uid}")
            auth.signOut()
            Log.d("AuthRepository", "Sign out successful")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign out failed", e)
            throw e
        }
    }

    override fun getCurrentUser(): FirebaseUser? {
        val user = auth.currentUser
        Log.d("AuthRepository", "Current user: ${user?.uid}")
        return user
    }

    override suspend fun getUserData(userId: String): Result<User> {
        return try {
            Log.d("AuthRepository", "Fetching user data for: $userId")
            val document = firestore.collection("users").document(userId).get().await()
            val user = document.toObject(User::class.java)
            if (user != null) {
                Log.d("AuthRepository", "User data fetched successfully")
                Result.success(user)
            } else {
                Log.e("AuthRepository", "User data not found for: $userId")
                Result.failure(Exception("User data not found"))
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Failed to fetch user data", e)
            Result.failure(e)
        }
    }
} 