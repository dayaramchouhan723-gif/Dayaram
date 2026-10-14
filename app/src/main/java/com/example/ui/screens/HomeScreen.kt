package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AgentProfile
import com.example.service.AiConnectorService
import com.example.service.KeyStorageManager
import com.example.service.SpeechService
import com.example.ui.components.ClaymorphicCard
import com.example.ui.components.ConnectorsCard
import com.example.ui.components.OrbWidget
import com.example.ui.components.VoiceCloneStudioCard
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    activeAgent: AgentProfile,
    keyStorage: KeyStorageManager,
    speechService: SpeechService,
    aiConnectorService: AiConnectorService? = null,
    onNavigateToChat: (initialPrompt: String?) -> Unit,
    onNavigateToVisualLens: () -> Unit,
    onNavigateToAuraControl: () -> Unit,
    onNavigateToAgentHub: () -> Unit,
    onNavigateToResumax: () -> Unit,
    onStartVoiceMode: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(EmpireBlack)
            .statusBarsPadding()
            .testTag("home_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Top Header: Greeting "Hello, CHOUHAN STAR 👑" + Active Agent Badge
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Hello, CHOUHAN STAR",
                            color = TextWhite,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "👑", fontSize = 20.sp)
                    }
                    Text(
                        text = "System 2.2.12 | ${activeAgent.name} Active",
                        color = activeAgent.orbColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Mini Agent Orb Avatar that navigates to Hub
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(EmpireCardElevated)
                        .border(1.5.dp, activeAgent.orbColor, CircleShape)
                        .clickable { onNavigateToAgentHub() }
                        .testTag("agent_avatar_button"),
                    contentAlignment = Alignment.Center
                ) {
                    OrbWidget(
                        size = 38.dp,
                        baseColor = activeAgent.orbColor,
                        secondaryColor = ImperialGold,
                        showCrown = false,
                        showParticles = false,
                        ringCount = 2,
                        orbType = "Neon"
                    )
                }
            }
        }

        // 2. Search bar: "Ask CHOUHAN EMPIRE anything..."
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Ask CHOUHAN EMPIRE anything...",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = NeonRed
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (searchQuery.isNotBlank()) {
                                onNavigateToChat(searchQuery)
                            } else {
                                onStartVoiceMode()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (searchQuery.isNotBlank()) Icons.Default.Send else Icons.Default.Mic,
                            contentDescription = "Action",
                            tint = ImperialGold
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("empire_search_bar"),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonRed,
                    unfocusedBorderColor = EmpireCardBorder,
                    focusedContainerColor = EmpireCardBg,
                    unfocusedContainerColor = EmpireCardBg,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    if (searchQuery.isNotBlank()) {
                        onNavigateToChat(searchQuery)
                    }
                })
            )
        }

        // 2A. CONNECTORS Card UI (3 fields: ElevenLabs, Gemini, Groq -> EncryptedSharedPreferences)
        item {
            ConnectorsCard(
                keyStorage = keyStorage
            )
        }

        // 2B. Voice Clone Studio Card (Record 10s -> "Jai Mata Di Boss" -> Local Voice ID)
        item {
            VoiceCloneStudioCard(
                keyStorage = keyStorage,
                speechService = speechService,
                aiConnectorService = aiConnectorService
            )
        }

        // 3. Featured Active Agent Banner (e.g. RESUMAX or MYRA)
        item {
            ClaymorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = activeAgent.orbColor,
                glowColor = activeAgent.orbColor,
                borderWidth = 1.5.dp,
                elevation = 8.dp,
                onClick = {
                    if (activeAgent.id == "resumax") {
                        onNavigateToResumax()
                    } else {
                        onNavigateToChat(null)
                    }
                },
                testTag = "active_agent_hero_card"
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = activeAgent.name,
                                color = TextWhite,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (activeAgent.isNew) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(ImperialGold, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "NEW",
                                        color = EmpireBlack,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        Text(
                            text = activeAgent.description,
                            color = TextMuted,
                            fontSize = 13.sp,
                            maxLines = 2,
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Tools: ${activeAgent.toolsCount}",
                                color = ImperialGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Switch Agent →",
                                color = activeAgent.orbColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    OrbWidget(
                        size = 85.dp,
                        baseColor = activeAgent.orbColor,
                        secondaryColor = ImperialGold,
                        showCrown = true,
                        showParticles = true,
                        ringCount = 3,
                        orbType = "Neon"
                    )
                }
            }
        }

        // 4. 4 Core Feature Cards:
        // Voice Mode ("Talk to EMPIRE - Always on")
        // Visual Lens ("See & Analyze - Capture the world")
        // JARVIS Mode ("PC Assistant")
        // Aura Control ("System Control")
        item {
            Text(
                text = "EMPIRE OPERATIONS",
                color = ImperialGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Voice Mode
                    ClaymorphicCard(
                        modifier = Modifier.weight(1f),
                        borderColor = NeonRed,
                        glowColor = NeonRed,
                        onClick = onStartVoiceMode,
                        testTag = "voice_mode_card"
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(NeonRed.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Mode",
                                    tint = NeonRed
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Voice Mode",
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Talk to EMPIRE - Always on",
                                color = TextMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    // Visual Lens
                    ClaymorphicCard(
                        modifier = Modifier.weight(1f),
                        borderColor = ImperialGold,
                        glowColor = ImperialGold,
                        onClick = onNavigateToVisualLens,
                        testTag = "visual_lens_card"
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(ImperialGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CenterFocusStrong,
                                    contentDescription = "Visual Lens",
                                    tint = ImperialGold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Visual Lens",
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "See & Analyze - Capture the world",
                                color = TextMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // JARVIS Mode
                    ClaymorphicCard(
                        modifier = Modifier.weight(1f),
                        borderColor = AgentCyan,
                        glowColor = AgentCyan,
                        onClick = { onNavigateToChat("JARVIS System Telemetry Report") },
                        testTag = "jarvis_mode_card"
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(AgentCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Computer,
                                    contentDescription = "JARVIS Mode",
                                    tint = AgentCyan
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "JARVIS Mode",
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "PC Assistant - 4 AI Core",
                                color = TextMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    // Aura Control
                    ClaymorphicCard(
                        modifier = Modifier.weight(1f),
                        borderColor = NeonRed,
                        glowColor = NeonRed,
                        onClick = onNavigateToAuraControl,
                        testTag = "aura_control_card"
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(NeonRed.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Aura Control",
                                    tint = NeonRed
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Aura Control",
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "System Control & Haptics",
                                color = TextMuted,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. Quick Actions: "Deep Research + Image Search"
        item {
            ClaymorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = EmpireCardBorder,
                onClick = { onNavigateToChat("Execute Deep Research on AI agent architectures") },
                testTag = "quick_actions_card"
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Quick Action",
                            tint = ImperialGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Quick Actions",
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Deep Research + Image Search",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(NeonRedDark, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "LAUNCH",
                            color = NeonRedGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 6. Today's Insight Card
        item {
            ClaymorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = ImperialGoldDark,
                glowColor = ImperialGold,
                testTag = "todays_insight_card"
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "👑", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TODAY'S EMPIRE INSIGHT",
                                color = ImperialGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "VER 2.2.12",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "RESUMAX Career AI is newly deployed to the fleet. Optimize your resume ATS score to 85+ and target international opportunities in Dubai and globally with zero friction.",
                        color = TextOffWhite,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onNavigateToResumax,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ImperialGold,
                            contentColor = EmpireBlack
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text(
                            text = "Launch RESUMAX ATS Scanner",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
