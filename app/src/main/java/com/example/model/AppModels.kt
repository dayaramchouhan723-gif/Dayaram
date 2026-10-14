package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.NeonRed
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.AgentCyan
import com.example.ui.theme.AgentGreen

data class ChatMessage(
    val id: String,
    val sender: String, // "user" or agent name
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isAgent: Boolean = false,
    val agentOrbColor: Color = NeonRed
)

enum class AuraSignature(val displayName: String, val primaryColor: Color, val secondaryColor: Color) {
    CRIMSON("Crimson Aura", NeonRed, ImperialGold),
    IMPERIAL_GOLD("Imperial Gold", ImperialGold, NeonRed),
    CYBER_NEON("Cyber Neon", AgentCyan, NeonRed),
    EMERALD_PULSE("Emerald Pulse", AgentGreen, ImperialGold)
}

enum class LauncherCoreIdentity(val displayName: String, val subtitle: String) {
    DEFAULT("Default", "Original Imperial Crown"),
    AURA_RED("Aura Red", "High-Energy Crimson Core"),
    MIDNIGHT("Midnight", "Stealth Onyx Silhouette")
}

data class ConnectorItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val isConnected: Boolean,
    val category: String,
    val badge: String = "Free Personal",
    val latencyMs: Int = 42,
    val apiKeyMasked: String = "None Required"
)
