package com.example.service

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.example.model.AgentProfile
import com.example.model.AgentProfiles
import com.example.model.ChatMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class AgentManager(
    private val context: Context,
    private val speechService: SpeechService,
    private val aiConnectorService: AiConnectorService? = null
) {
    private val prefs = context.getSharedPreferences("empire_agent_memories", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.Main)

    // Current active agent
    private val _activeAgent = MutableStateFlow(AgentProfiles.getAgentById("resumax"))
    val activeAgent: StateFlow<AgentProfile> = _activeAgent.asStateFlow()

    // Chat messages for current session
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    init {
        // Load default greeting
        val initialAgent = _activeAgent.value
        _chatMessages.value = listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = initialAgent.name,
                text = "${initialAgent.statusGreeting} How can I empower you today, Commander?",
                isAgent = true,
                agentOrbColor = initialAgent.orbColor
            )
        )
    }

    fun switchAgent(agent: AgentProfile) {
        _activeAgent.value = agent
        // Load agent memory greeting
        val memoryData = loadAgentMemory(agent.memoryFile)
        val greeting = if (memoryData.isNotEmpty()) {
            "${agent.statusGreeting} Memory synchronized: ${memoryData.firstOrNull() ?: "Ready."}"
        } else {
            "${agent.statusGreeting} Switched to ${agent.name} (${agent.role})."
        }

        _chatMessages.value = _chatMessages.value + ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = agent.name,
            text = greeting,
            isAgent = true,
            agentOrbColor = agent.orbColor
        )

        // Vocal greeting announcement
        val announceText = "${agent.name} activated."
        if (aiConnectorService != null) {
            scope.launch {
                aiConnectorService.speakWithElevenLabs(announceText) { fallback ->
                    speechService.speak(fallback)
                }
            }
        } else {
            speechService.speak(announceText)
        }
    }

    fun sendMessage(userText: String, onComplete: (() -> Unit)? = null) {
        if (userText.isBlank()) return

        val agent = _activeAgent.value
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = "CHOUHAN STAR",
            text = userText,
            isAgent = false
        )
        _chatMessages.value = _chatMessages.value + userMsg

        _isGenerating.value = true

        scope.launch {
            val replyText = if (aiConnectorService != null) {
                try {
                    aiConnectorService.generateAgentReply(agent, userText)
                } catch (e: Exception) {
                    generateAgentResponse(agent, userText)
                }
            } else {
                generateAgentResponse(agent, userText)
            }

            val agentMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = agent.name,
                text = replyText,
                isAgent = true,
                agentOrbColor = agent.orbColor
            )
            _chatMessages.value = _chatMessages.value + agentMsg
            _isGenerating.value = false

            // Save exchange to agent's memory json
            recordMemory(agent.memoryFile, "User asked: '$userText' -> ${agent.name} responded.")

            // Speak response if brief or requested using ElevenLabs if key present
            val voiceSnippet = replyText.take(250)
            if (aiConnectorService != null) {
                aiConnectorService.speakWithElevenLabs(voiceSnippet) { fallback ->
                    speechService.speak(fallback)
                }
            } else {
                speechService.speak(voiceSnippet)
            }
            onComplete?.invoke()
        }
    }

    private fun generateAgentResponse(agent: AgentProfile, query: String): String {
        val q = query.lowercase()
        return when (agent.id) {
            "resumax" -> {
                when {
                    q.contains("score") || q.contains("ats") -> 
                        "RESUMAX ATS Engine: Candidate baseline is 85/100. Upload your resume or select a preset to analyze action verbs, keyword density, and generate an executive cover letter."
                    q.contains("cover letter") -> 
                        "RESUMAX Cover Letter Generator: Ready to draft an elite, high-conversion cover letter. Specify your target employer and role!"
                    else -> 
                        "RESUMAX active. I can parse PDFs, audit keyword saturation, score ATS compatibility 0-100, and coach your executive interview presence."
                }
            }
            "myra" -> {
                "Hello Boss! I understand how busy your schedule gets. Remember that your Empire is built one mindful decision at a time. I'm keeping your reminders and goals safe."
            }
            "kayra" -> {
                "KAYRA Strategy Core: Analyzing market vectors. Dubai tech corridors show high demand for lead mobile architects. WhatsApp lead workflows and OTP monitoring protocols are primed."
            }
            "maya" -> {
                "Good afternoon Boss! Ambient mood is sublime today. I recommend contrasting crimson neon #FF1A1A with imperial gold #FFD700 and claymorphic soft-curved surfaces."
            }
            "jarvis" -> {
                "JARVIS PC Assistant: Neural bridge established. CPU load at 14%, 4 AI cores synchronized. Terminal ready to execute remote telemetry commands."
            }
            "zoya" -> {
                "ZOYA Research: Continuously monitoring academic papers and live market feeds. Latest synthesis indicates zero latency in local AI inference."
            }
            "vyron" -> {
                "VYRON Compiler: Kotlin Coroutines and Jetpack Compose architecture verified. Zero memory leaks detected. Ready to architect clean modules."
            }
            "archer" -> {
                "ARCHER Navigation: Geospatial trajectory locked. Global data streams mapped. Ready to plot optimal navigation coordinates."
            }
            "nova" -> {
                "NOVA Viral Engine: High-retention hooks prepared. Recommended hook: 'How 11 specialized AI agents in CHOUHAN EMPIRE transformed personal productivity in 24 hours.'"
            }
            "stonic" -> {
                "STONIC Logistics: Supply chain nodes online. Warehouse transit delays reduced by 18%. Fleet telemetry steady."
            }
            "luna" -> {
                "LUNA Night Guardian: Ambient lighting dimmed. Calming soundscapes ready. May tonight bring peaceful rejuvenation to your empire."
            }
            else -> {
                "${agent.name} is executing protocols within the CHOUHAN EMPIRE. Tools available: ${agent.tools.joinToString(", ")}."
            }
        }
    }

    private fun recordMemory(memoryFile: String, entry: String) {
        try {
            val raw = prefs.getString(memoryFile, "[]") ?: "[]"
            val array = JSONArray(raw)
            array.put(0, entry)
            prefs.edit().putString(memoryFile, array.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadAgentMemory(memoryFile: String): List<String> {
        val list = mutableListOf<String>()
        try {
            val raw = prefs.getString(memoryFile, "[]") ?: "[]"
            val array = JSONArray(raw)
            for (i in 0 until array.length().coerceAtMost(4)) {
                list.add(array.getString(i))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
