package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.FirebaseResumeRecord
import com.example.model.FirebaseUserPreferences
import com.example.model.ResumeScanResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

class EmpireFirestoreRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        FirebaseAuth.getInstance()
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun logError(op: String, path: String, e: Exception) {
        val json = JSONObject().apply {
            put("error", e.message)
            put("operation", op)
            put("path", path)
            put("timestamp", System.currentTimeMillis())
        }
        Log.e("FirestoreError", json.toString(), e)
    }

    fun observeResumes(): Flow<List<FirebaseResumeRecord>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/resumes"
        try {
            emitAll(
                db.collection("users").document(uid).collection("resumes")
                    .snapshots()
                    .map { snapshot -> snapshot.toObjects(FirebaseResumeRecord::class.java) }
            )
        } catch (e: Exception) {
            logError("LIST", path, e)
            throw e
        }
    }

    suspend fun saveResume(result: ResumeScanResult): String {
        val uid = requireUserId()
        val collectionRef = db.collection("users").document(uid).collection("resumes")
        val payload = hashMapOf(
            "userId" to uid,
            "candidateName" to result.candidateName,
            "targetJobTitle" to result.targetJobTitle,
            "overallScore" to result.overallScore,
            "impactScore" to result.impactScore,
            "skillsScore" to result.skillsScore,
            "formatScore" to result.formatScore,
            "verbScore" to result.verbScore,
            "executiveSummary" to result.executiveSummary,
            "vocalScript" to result.vocalScript,
            "generatedCoverLetter" to result.generatedCoverLetter,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            val docRef = collectionRef.add(payload).await()
            docRef.id
        } catch (e: Exception) {
            logError("CREATE", collectionRef.path, e)
            throw e
        }
    }

    fun observePreferences(): Flow<FirebaseUserPreferences?> = flow {
        val uid = requireUserId()
        val path = "users/$uid/preferences"
        try {
            emitAll(
                db.collection("users").document(uid).collection("preferences")
                    .document("current")
                    .snapshots()
                    .map { snapshot ->
                        if (snapshot.exists()) snapshot.toObject(FirebaseUserPreferences::class.java) else null
                    }
            )
        } catch (e: Exception) {
            logError("GET", path, e)
            throw e
        }
    }

    suspend fun savePreferences(
        activeAgentId: String,
        auraSignature: String = "Crimson Aura",
        launcherCore: String = "Default",
        hapticsEnabled: Boolean = true,
        orbType: String = "Neon",
        orbSize: Double = 1.0
    ) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("preferences").document("current")
        val payload = hashMapOf(
            "userId" to uid,
            "activeAgentId" to activeAgentId,
            "auraSignature" to auraSignature,
            "launcherCore" to launcherCore,
            "hapticsEnabled" to hapticsEnabled,
            "orbType" to orbType,
            "orbSize" to orbSize,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            logError("SET", docRef.path, e)
            throw e
        }
    }
}
