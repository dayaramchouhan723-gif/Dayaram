package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AgentProfile
import com.example.service.AgentManager
import com.example.service.SpeechService
import com.example.ui.components.ClaymorphicCard
import com.example.ui.components.OrbWidget
import com.example.ui.theme.*

@Composable
fun ChatScreen(
    agentManager: AgentManager,
    speechService: SpeechService,
    onBack: () -> Unit,
    initialPrompt: String? = null
) {
    val activeAgent by agentManager.activeAgent.collectAsState()
    val messages by agentManager.chatMessages.collectAsState()
    val isGenerating by agentManager.isGenerating.collectAsState()
    val isSpeaking by speechService.isSpeaking.collectAsState()

    var inputText by remember { mutableStateOf(initialPrompt ?: "") }

    LaunchedEffect(initialPrompt) {
        if (!initialPrompt.isNullOrBlank()) {
            agentManager.sendMessage(initialPrompt)
            inputText = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EmpireBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("chat_screen")
    ) {
        // 1. Top Header with Active Agent Orb and Role
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ImperialGold
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            OrbWidget(
                size = 42.dp,
                baseColor = activeAgent.orbColor,
                secondaryColor = ImperialGold,
                showCrown = false,
                showParticles = false,
                ringCount = 2,
                orbType = "Neon",
                isSpeaking = isSpeaking
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = activeAgent.name,
                        color = TextWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "👑", fontSize = 14.sp)
                }
                Text(
                    text = "${activeAgent.role} • 0₹ Lifetime Free",
                    color = activeAgent.orbColor,
                    fontSize = 12.sp
                )
            }

            IconButton(
                onClick = {
                    val lastAgentMsg = messages.lastOrNull { it.isAgent }
                    lastAgentMsg?.let { speechService.speak(it.text) }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Read aloud",
                    tint = ImperialGold
                )
            }
        }

        // 2. Quick Tool Pills for active agent
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(activeAgent.tools) { toolName ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmpireCardElevated)
                        .border(1.dp, activeAgent.orbColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable {
                            agentManager.sendMessage("Execute tool: $toolName")
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = toolName,
                        color = TextOffWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Divider(
            color = EmpireCardBorder,
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // 3. Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            reverseLayout = false
        ) {
            items(messages, key = { it.id }) { msg ->
                val isMe = !msg.isAgent
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    if (!isMe) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(msg.agentOrbColor.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "👑", fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    ClaymorphicCard(
                        modifier = Modifier.widthIn(max = 290.dp),
                        borderColor = if (isMe) NeonRed else msg.agentOrbColor.copy(alpha = 0.6f),
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = msg.sender,
                                color = if (isMe) NeonRed else msg.agentOrbColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.text,
                                color = TextWhite,
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            if (isGenerating) {
                item {
                    Text(
                        text = "${activeAgent.name} is formulating response...",
                        color = ImperialGold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 38.dp)
                    )
                }
            }
        }

        // 4. Input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EmpireCardBg)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Message ${activeAgent.name}...", color = TextMuted, fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                shape = RoundedCornerShape(22.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = activeAgent.orbColor,
                    unfocusedBorderColor = EmpireCardBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        agentManager.sendMessage(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(activeAgent.orbColor)
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = EmpireBlack
                )
            }
        }
    }
}
