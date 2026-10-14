package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AuraSignature
import com.example.model.LauncherCoreIdentity
import com.example.service.AuraManager
import com.example.ui.components.ClaymorphicCard
import com.example.ui.theme.*

@Composable
fun AuraControlScreen(
    auraManager: AuraManager,
    onBack: () -> Unit
) {
    val auraSignature by auraManager.auraSignature.collectAsState()
    val launcherCore by auraManager.launcherCore.collectAsState()
    val hapticsEnabled by auraManager.hapticsEnabled.collectAsState()
    val cyberShield by auraManager.cyberShield.collectAsState()
    val ultraPerformance by auraManager.ultraPerformance.collectAsState()
    val whisperVoice by auraManager.whisperVoice.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(EmpireBlack)
            .statusBarsPadding()
            .testTag("aura_control_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. AppBar AURA CONTROL
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ImperialGold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "AURA CONTROL",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "System Telemetry & Identity 2.2.10",
                        color = NeonRed,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 2. Visual Identity Launcher Core "Select your unique app identity"
        // 3 icons: Default / Aura Red / Midnight (Default selected red border)
        item {
            ClaymorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonRed,
                testTag = "launcher_core_card"
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "VISUAL IDENTITY LAUNCHER CORE",
                        color = ImperialGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Select your unique app identity",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LauncherCoreIdentity.values().forEach { core ->
                            val isSelected = launcherCore == core
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(EmpireCardElevated)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) NeonRed else EmpireCardBorder,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { auraManager.setLauncherCore(core) }
                                    .padding(vertical = 14.dp, horizontal = 8.dp)
                                    .testTag("launcher_core_${core.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = when (core) {
                                            LauncherCoreIdentity.DEFAULT -> "👑"
                                            LauncherCoreIdentity.AURA_RED -> "🔴"
                                            LauncherCoreIdentity.MIDNIGHT -> "🌑"
                                        },
                                        fontSize = 26.sp
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = core.displayName,
                                        color = if (isSelected) TextWhite else TextMuted,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (isSelected) {
                                        Text(
                                            text = "SELECTED",
                                            color = NeonRed,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Core Interaction Aura Signature: "Current: Crimson Aura"
        item {
            ClaymorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = auraSignature.primaryColor,
                glowColor = auraSignature.primaryColor,
                testTag = "aura_signature_card"
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CORE INTERACTION AURA SIGNATURE",
                                color = ImperialGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Current: ${auraSignature.displayName}",
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(auraSignature.primaryColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AuraSignature.values().forEach { sig ->
                            val isCurrent = auraSignature == sig
                            Button(
                                onClick = { auraManager.setAuraSignature(sig) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isCurrent) sig.primaryColor else EmpireCardElevated,
                                    contentColor = if (isCurrent) EmpireBlack else TextWhite
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = sig.displayName.split(" ").first(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Haptic Feedback toggle ON red
        item {
            ClaymorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (hapticsEnabled) NeonRed else EmpireCardBorder,
                testTag = "haptic_toggle_card"
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Haptic Feedback",
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tactile vibration confirmation on interaction",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = hapticsEnabled,
                        onCheckedChange = { auraManager.setHapticsEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextWhite,
                            checkedTrackColor = NeonRed,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = EmpireCardElevated
                        ),
                        modifier = Modifier.testTag("haptic_switch")
                    )
                }
            }
        }

        // 5. System Modes
        item {
            Text(
                text = "SYSTEM OPERATION MODES",
                color = ImperialGold,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SystemModeRow(
                    title = "Cyber Shield 2.2.10",
                    desc = "Zero data leakage, local sandbox encrypted memory",
                    isActive = cyberShield,
                    onToggle = { auraManager.toggleCyberShield() }
                )
                SystemModeRow(
                    title = "Ultra Performance Overclock",
                    desc = "Zero-latency parallel neural inferences",
                    isActive = ultraPerformance,
                    onToggle = { auraManager.toggleUltraPerformance() }
                )
                SystemModeRow(
                    title = "Whisper Voice Mode",
                    desc = "Subtle vocal responses & quiet telemetry notifications",
                    isActive = whisperVoice,
                    onToggle = { auraManager.toggleWhisperVoice() }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SystemModeRow(
    title: String,
    desc: String,
    isActive: Boolean,
    onToggle: () -> Unit
) {
    ClaymorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (isActive) NeonRed else EmpireCardBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = desc, color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
            }
            Switch(
                checked = isActive,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TextWhite,
                    checkedTrackColor = NeonRed,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = EmpireCardElevated
                )
            )
        }
    }
}
