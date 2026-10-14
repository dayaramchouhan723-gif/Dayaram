package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirebaseResumeRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val candidateName: String = "",
    val targetJobTitle: String = "",
    val overallScore: Int = 0,
    val impactScore: Int = 0,
    val skillsScore: Int = 0,
    val formatScore: Int = 0,
    val verbScore: Int = 0,
    val executiveSummary: String = "",
    val vocalScript: String = "",
    val generatedCoverLetter: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirebaseUserPreferences(
    @DocumentId val id: String = "",
    val userId: String = "",
    val activeAgentId: String = "resumax",
    val auraSignature: String = "Crimson Aura",
    val launcherCore: String = "Default",
    val hapticsEnabled: Boolean = true,
    val orbType: String = "Neon",
    val orbSize: Double = 1.0,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
