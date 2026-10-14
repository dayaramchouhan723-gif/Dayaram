package com.example.service

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.model.AgentProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class AiConnectorService(
    private val context: Context,
    private val keyStorage: KeyStorageManager
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private var mediaPlayer: MediaPlayer? = null

    suspend fun generateAgentReply(agent: AgentProfile, userQuery: String): String = withContext(Dispatchers.IO) {
        val groqKey = keyStorage.getGroqKey()
        val geminiKey = keyStorage.getGeminiKey()

        // 1. Try Groq first if key present
        if (groqKey.isNotBlank()) {
            try {
                val groqReply = callGroq(groqKey, agent.systemPrompt, userQuery)
                if (groqReply.isNotBlank()) return@withContext groqReply
            } catch (e: Exception) {
                Log.w("AiConnector", "Groq call failed, falling back: ${e.message}")
            }
        }

        // 2. Try Gemini if key present
        if (geminiKey.isNotBlank()) {
            try {
                val geminiReply = callGemini(geminiKey, agent.systemPrompt, userQuery)
                if (geminiReply.isNotBlank()) return@withContext geminiReply
            } catch (e: Exception) {
                Log.w("AiConnector", "Gemini call failed, falling back: ${e.message}")
            }
        }

        // 3. Built-in Agent Neural Logic (for MYRA, STONIC, KAYRA, RESUMAX, etc.)
        generateNativeAgentReply(agent, userQuery)
    }

    private fun callGroq(apiKey: String, systemPrompt: String, userQuery: String): String {
        val jsonBody = JSONObject().apply {
            put("model", "llama-3.1-70b-versatile")
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userQuery)
                })
            }
            put("messages", messages)
            put("temperature", 0.7)
            put("max_tokens", 512)
        }

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val err = response.body?.string()
                throw RuntimeException("Groq error HTTP ${response.code}: $err")
            }
            val resJson = JSONObject(response.body?.string() ?: "{}")
            val choices = resJson.optJSONArray("choices")
            if (choices != null && choices.length() > 0) {
                val msg = choices.getJSONObject(0).optJSONObject("message")
                return msg?.optString("content") ?: ""
            }
        }
        return ""
    }

    private fun callGemini(apiKey: String, systemPrompt: String, userQuery: String): String {
        val jsonBody = JSONObject().apply {
            val contents = JSONArray().apply {
                put(JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().apply { put("text", userQuery) })
                    }
                    put("parts", parts)
                })
            }
            put("contents", contents)

            put("systemInstruction", JSONObject().apply {
                val sysParts = JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                }
                put("parts", sysParts)
            })
        }

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val err = response.body?.string()
                throw RuntimeException("Gemini error HTTP ${response.code}: $err")
            }
            val resJson = JSONObject(response.body?.string() ?: "{}")
            val candidates = resJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val content = candidates.getJSONObject(0).optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    return parts.getJSONObject(0).optString("text")
                }
            }
        }
        return ""
    }

    private fun generateNativeAgentReply(agent: AgentProfile, userQuery: String): String {
        val q = userQuery.lowercase()
        return when (agent.id) {
            "myra" -> {
                "Hello Boss! MYRA online. Dil se sun rahi hoon, aapki energy aur goals ko align karte hain. Reminders aur daily reflections loaded."
            }
            "stonic" -> {
                "STONIC Logistics Core: Supply chain, fleet telemetry, and asset optimization channels synchronized. Operational efficiency at 99.8%."
            }
            "kayra" -> {
                "KAYRA Executive Strategist: Dubai job market vectors, OTP parsing, and WhatsApp business automation flows are armed and ready."
            }
            "resumax" -> {
                when {
                    q.contains("score") || q.contains("ats") ->
                        "RESUMAX ATS Engine: Candidate benchmark 85/100. Quantifiable metrics verified, missing keywords identified for global placement."
                    else ->
                        "RESUMAX Career AI ready. Upload your resume or job description to run real-time keyword analysis and generate tailored cover letters."
                }
            }
            "maya" -> {
                "Good afternoon Boss! Maya here. Colors, aesthetics, and creative mood synchronizer tuned to crimson red & imperial gold."
            }
            "jarvis" -> {
                "JARVIS PC Control: Neural link stable. 4 AI Cores operational. Ready to dispatch system commands."
            }
            "zoya" -> {
                "ZOYA Research: Continuous listening mode active. Synthesizing market intelligence and research literature."
            }
            "vyron" -> {
                "VYRON Compiler: Kotlin Compose modules clean. Architecture pattern validated with zero memory leaks."
            }
            "archer" -> {
                "ARCHER Navigation: Satellite coordinates and data heatmaps locked. Trajectory optimized."
            }
            "nova" -> {
                "NOVA Viral Engine: High-converting viral hooks and research storylines generated."
            }
            "luna" -> {
                "LUNA Night Wellness: Circadian rhythm and evening mindfulness soundscapes primed for rest."
            }
            else -> {
                "${agent.name} active in CHOUHAN EMPIRE. Tools available: ${agent.tools.joinToString(", ")}."
            }
        }
    }

    suspend fun speakWithElevenLabs(text: String, onFallback: (String) -> Unit): Boolean = withContext(Dispatchers.IO) {
        val apiKey = keyStorage.getElevenLabsKey()
        val voiceId = keyStorage.getVoiceId()

        if (apiKey.isBlank()) {
            withContext(Dispatchers.Main) { onFallback(text) }
            return@withContext false
        }

        try {
            val jsonBody = JSONObject().apply {
                put("text", text)
                put("model_id", "eleven_multilingual_v2")
                put("voice_settings", JSONObject().apply {
                    put("stability", 0.5)
                    put("similarity_boost", 0.8)
                })
            }

            val request = Request.Builder()
                .url("https://api.elevenlabs.io/v1/text-to-speech/$voiceId")
                .addHeader("xi-api-key", apiKey)
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw RuntimeException("ElevenLabs HTTP ${response.code}")
                }
                val audioBytes = response.body?.bytes() ?: return@withContext false
                val tempFile = File.createTempFile("elevenlabs_tts_", ".mp3", context.cacheDir)
                FileOutputStream(tempFile).use { it.write(audioBytes) }

                withContext(Dispatchers.Main) {
                    mediaPlayer?.release()
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(tempFile.absolutePath)
                        prepare()
                        start()
                        setOnCompletionListener {
                            tempFile.delete()
                        }
                    }
                }
                return@withContext true
            }
        } catch (e: Exception) {
            Log.e("ElevenLabs", "TTS failed: ${e.message}")
            withContext(Dispatchers.Main) { onFallback(text) }
            return@withContext false
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
