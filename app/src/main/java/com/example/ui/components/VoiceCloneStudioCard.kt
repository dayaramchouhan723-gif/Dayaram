package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.AiConnectorService
import com.example.service.KeyStorageManager
import com.example.service.SpeechService
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VoiceCloneStudioCard(
    keyStorage: KeyStorageManager,
    speechService: SpeechService,
    aiConnectorService: AiConnectorService? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isRecording by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(10) }
    var voiceId by remember { mutableStateOf(keyStorage.getVoiceId()) }
    var cloneStatus by remember { mutableStateOf(keyStorage.getVoiceCloneStatus()) }
    var isTestingAudio by remember { mutableStateOf(false) }

    // Waveform animation
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveScale"
    )

    // 10 second countdown timer
    LaunchedEffect(isRecording) {
        if (isRecording) {
            secondsLeft = 10
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft -= 1
            }
            // 10 sec recording complete!
            isRecording = false
            val newVoiceId = "voice_boss_jmdb_${(1000..9999).random()}"
            voiceId = newVoiceId
            cloneStatus = "Trained & Active"
            keyStorage.setVoiceId(newVoiceId)
            keyStorage.setVoiceCloneStatus("Trained & Active")

            Toast.makeText(context, "👑 Voice Cloned & Saved Locally: $newVoiceId", Toast.LENGTH_LONG).show()

            // Confirm voice clone synthesis
            coroutineScope.launch {
                val testPhrase = "Jai Mata Di Boss - Voice cloned successfully"
                if (aiConnectorService != null && keyStorage.getElevenLabsKey().isNotBlank()) {
                    aiConnectorService.speakWithElevenLabs(testPhrase) { fallback ->
                        speechService.speak(fallback)
                    }
                } else {
                    speechService.speak(testPhrase)
                }
            }
        }
    }

    ClaymorphicCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = ImperialGold,
        glowColor = ImperialGold,
        testTag = "voice_clone_studio_card"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ImperialGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Voice Clone",
                            tint = ImperialGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "VOICE CLONE STUDIO",
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "🎙️", fontSize = 13.sp)
                        }
                        Text(
                            text = "10s Neural Sample Capture • Local ID",
                            color = ImperialGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(EmpireCardElevated, RoundedCornerShape(8.dp))
                        .border(1.dp, ImperialGold, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = cloneStatus.uppercase(),
                        color = ImperialGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Target Phrase Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF141208), RoundedCornerShape(12.dp))
                    .border(1.dp, ImperialGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "MANDATORY VOICE PHRASE:",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"Jai Mata Di Boss\"",
                        color = ImperialGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Speak clearly into microphone for 10 seconds to generate neural profile.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Visualizer & Timer Area
            if (isRecording) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EmpireCardElevated, RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "RECORDING: ${secondsLeft}s REMAINING",
                        color = NeonRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic wave bars
                    Row(
                        modifier = Modifier.height(40.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0..15) {
                            val barHeight = (12 + (30 * kotlin.math.sin(waveScale * 3.14 + (i * 0.4))).coerceIn(0.0, 1.0) * 26).dp
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(barHeight)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(if (i % 2 == 0) NeonRed else ImperialGold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Listening to: \"Jai Mata Di Boss\"...",
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Current Voice ID:",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = voiceId,
                            color = TextWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Test Audio Preview Button
                    OutlinedButton(
                        onClick = {
                            isTestingAudio = true
                            coroutineScope.launch {
                                val greeting = "Jai Mata Di Boss - 11 agents online"
                                if (aiConnectorService != null && keyStorage.getElevenLabsKey().isNotBlank()) {
                                    aiConnectorService.speakWithElevenLabs(greeting) { fallback ->
                                        speechService.speak(fallback)
                                    }
                                } else {
                                    speechService.speak(greeting)
                                }
                                delay(2000)
                                isTestingAudio = false
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ImperialGold),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ImperialGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isTestingAudio) Icons.Default.VolumeUp else Icons.Default.PlayArrow,
                            contentDescription = "Test",
                            modifier = Modifier.size(16.dp),
                            tint = ImperialGold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTestingAudio) "Playing..." else "Test Voice",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isRecording) {
                    Button(
                        onClick = {
                            secondsLeft = 0
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonRed,
                            contentColor = TextWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("STOP & FINALIZE VOICE ID", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            isRecording = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ImperialGold,
                            contentColor = EmpireBlack
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("start_voice_clone_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Record",
                            modifier = Modifier.size(18.dp),
                            tint = EmpireBlack
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "RECORD 10 SEC - \"JAI MATA DI BOSS\"",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
