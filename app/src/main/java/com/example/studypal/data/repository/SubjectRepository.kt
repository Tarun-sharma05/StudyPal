package com.example.studypal.data.repository

import com.example.studypal.model.Subject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import java.time.LocalDate

interface SubjectRepository {
    suspend fun addSubject(subject: Subject)
    suspend fun updateSubject(subject: Subject)
    suspend fun deleteSubject(subjectId: String)
    suspend fun getSubjects(): List<Subject>
}

@Singleton
class SubjectRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : SubjectRepository {

    override suspend fun addSubject(subject: Subject) {
        val userId = auth.currentUser?.uid
        Log.d("SubjectRepository", "Adding subject. Current user ID: $userId")
        Log.d("SubjectRepository", "Subject details - Name: ${subject.name}, Exam Date: ${subject.examDate}")
        
        if (userId == null) {
            Log.e("SubjectRepository", "User not authenticated")
            throw Exception("User not authenticated")
        }
        
        try {
            val subjectWithUserId = subject.copy(userId = userId)
            Log.d("SubjectRepository", "Saving subject to Firestore: $subjectWithUserId")
            
            val docRef = firestore.collection("subjects").document()
            Log.d("SubjectRepository", "Created document reference: ${docRef.id}")
            
            val subjectMap = mapOf(
                "id" to docRef.id,
                "name" to subjectWithUserId.name,
                "examDate" to subjectWithUserId.examDate?.toString(),
                "userId" to userId,
                "createdAt" to com.google.firebase.Timestamp.now()
            )
            
            Log.d("SubjectRepository", "Saving subject data: $subjectMap")
            docRef.set(subjectMap).await()
            Log.d("SubjectRepository", "Subject saved successfully to document: ${docRef.id}")
            
            // Verify the save by reading it back
            val savedDoc = docRef.get().await()
            Log.d("SubjectRepository", "Verification - Read back document: ${savedDoc.data}")
        } catch (e: Exception) {
            Log.e("SubjectRepository", "Error saving subject to Firestore", e)
            throw e
        }
    }

    override suspend fun updateSubject(subject: Subject) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        if (subject.userId != userId) {
            throw Exception("Unauthorized access")
        }
        firestore.collection("subjects")
            .document(subject.id)
            .set(subject)
            .await()
    }

    override suspend fun deleteSubject(subjectId: String) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        val subject = firestore.collection("subjects")
            .document(subjectId)
            .get()
            .await()
            .toObject(Subject::class.java)
            ?: throw Exception("Subject not found")

        if (subject.userId != userId) {
            throw Exception("Unauthorized access")
        }

        firestore.collection("subjects")
            .document(subjectId)
            .delete()
            .await()
    }

    override suspend fun getSubjects(): List<Subject> {
        val userId = auth.currentUser?.uid
        Log.d("SubjectRepository", "Getting subjects. Current user ID: $userId")
        
        if (userId == null) {
            Log.e("SubjectRepository", "User not authenticated")
            throw Exception("User not authenticated")
        }
        
        try {
            Log.d("SubjectRepository", "Querying Firestore for subjects with userId: $userId")
            val snapshot = firestore.collection("subjects")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            Log.d("SubjectRepository", "Firestore query returned ${snapshot.documents.size} documents")
            snapshot.documents.forEach { doc ->
                Log.d("SubjectRepository", "Document ID: ${doc.id}, Data: ${doc.data}")
            }
            
            val subjects = snapshot.documents.mapNotNull { doc ->
                val data = doc.data
                if (data != null) {
                    try {
                        Subject(
                            id = data["id"] as? String ?: doc.id,
                            name = data["name"] as? String ?: "",
                            examDate = (data["examDate"] as? String)?.let { LocalDate.parse(it) },
                            userId = data["userId"] as? String ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e("SubjectRepository", "Error parsing subject from document ${doc.id}", e)
                        null
                    }
                } else {
                    Log.e("SubjectRepository", "Document ${doc.id} has null data")
                    null
                }
            }
            
            Log.d("SubjectRepository", "Converted to ${subjects.size} subjects: $subjects")
            return subjects
        } catch (e: Exception) {
            Log.e("SubjectRepository", "Error fetching subjects from Firestore", e)
            throw e
        }
    }
} 