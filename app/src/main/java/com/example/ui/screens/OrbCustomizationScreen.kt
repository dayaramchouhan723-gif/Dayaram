package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.service.AuraManager
import com.example.ui.components.ClaymorphicCard
import com.example.ui.components.OrbWidget
import com.example.ui.theme.*

@Composable
fun OrbCustomizationScreen(
    auraManager: AuraManager,
    onBack: () -> Unit
) {
    val orbType by auraManager.orbType.collectAsState()
    val orbSize by auraManager.orbSizeMultiplier.collectAsState()
    val auraBorderMode by auraManager.auraBorderMode.collectAsState()
    val voiceVisualizer by auraManager.voiceVisualizerEnabled.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(EmpireBlack)
            .statusBarsPadding()
            .testTag("orb_customization_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Title Orb Customization
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
                        text = "ORB CUSTOMIZATION",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Real-Time Plasma & Spectral Synthesis",
                        color = NeonRed,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 2. Center green+blue neon rings orb
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                OrbWidget(
                    size = (180 * orbSize).dp,
                    baseColor = AgentGreen,
                    secondaryColor = AgentCyan,
                    showCrown = true,
                    showParticles = true,
                    ringCount = 5,
                    orbType = orbType,
                    isSpeaking = voiceVisualizer
                )
            }
        }

        // 3. Orb Type selector: Classic / Energy / Neon (Neon selected red)
        item {
            ClaymorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonRed,
                testTag = "orb_type_card"
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "ORB PLASMA TYPE",
                        color = ImperialGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("Classic", "Energy", "Neon").forEach { type ->
                            val isSelected = orbType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) NeonRedDark else EmpireCardElevated)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) NeonRed else EmpireCardBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { auraManager.setOrbType(type) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type,
                                    color = if (isSelected) TextWhite else TextMuted,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Adjust Orb Size slider red
        item {
            ClaymorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = EmpireCardBorder,
                testTag = "orb_size_card"
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ADJUST ORB SIZE",
                            color = ImperialGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${(orbSize * 100).toInt()}%",
                            color = NeonRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Slider(
                        value = orbSize,
                        onValueChange = { auraManager.setOrbSizeMultiplier(it) },
                        valueRange = 0.65f..1.35f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonRed,
                            activeTrackColor = NeonRed,
                            inactiveTrackColor = EmpireCardElevated
                        ),
                        modifier = Modifier.testTag("orb_size_slider")
                    )
                }
            }
        }

        // 5. System Presence Aura Border Mode ON toggle
        item {
            ClaymorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (auraBorderMode) NeonRed else EmpireCardBorder
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
                            text = "System Presence Aura Border Mode",
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Emits atmospheric red glow around app screen frame",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = auraBorderMode,
                        onCheckedChange = { auraManager.setAuraBorderMode(it) },
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

        // 6. Voice Visualizer ON red toggle
        item {
            ClaymorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (voiceVisualizer) NeonRed else EmpireCardBorder
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
                            text = "Voice Visualizer",
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Dynamic frequency waves pulse within center core",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = voiceVisualizer,
                        onCheckedChange = { auraManager.setVoiceVisualizerEnabled(it) },
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

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
