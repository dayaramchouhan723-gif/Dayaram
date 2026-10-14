package com.example.service

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class KeyStorageManager(context: Context) {
    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "empire_secure_keys",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback for Robolectric or devices without KeyStore hardware
        context.getSharedPreferences("empire_secure_keys_fallback", Context.MODE_PRIVATE)
    }

    fun getElevenLabsKey(): String = prefs.getString("elevenlabs_key", "") ?: ""
    fun setElevenLabsKey(key: String) = prefs.edit().putString("elevenlabs_key", key.trim()).apply()

    fun getGeminiKey(): String = prefs.getString("gemini_key", "") ?: ""
    fun setGeminiKey(key: String) = prefs.edit().putString("gemini_key", key.trim()).apply()

    fun getGroqKey(): String = prefs.getString("groq_key", "") ?: ""
    fun setGroqKey(key: String) = prefs.edit().putString("groq_key", key.trim()).apply()

    fun getVoiceId(): String = prefs.getString("voice_id", "21m00Tcm4TlvDq8ikWAM") ?: "21m00Tcm4TlvDq8ikWAM"
    fun setVoiceId(id: String) = prefs.edit().putString("voice_id", id.trim()).apply()

    fun getVoiceCloneStatus(): String = prefs.getString("voice_clone_status", "85% ready") ?: "85% ready"
    fun setVoiceCloneStatus(status: String) = prefs.edit().putString("voice_clone_status", status.trim()).apply()

    fun hasAnyKey(): Boolean = getElevenLabsKey().isNotBlank() || getGeminiKey().isNotBlank() || getGroqKey().isNotBlank()
}
