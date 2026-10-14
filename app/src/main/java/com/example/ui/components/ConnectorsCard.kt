package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.KeyStorageManager
import com.example.ui.theme.*

@Composable
fun ConnectorsCard(
    keyStorage: KeyStorageManager,
    modifier: Modifier = Modifier,
    onKeysUpdated: (() -> Unit)? = null
) {
    val context = LocalContext.current

    var elevenLabsKey by remember { mutableStateOf(keyStorage.getElevenLabsKey()) }
    var geminiKey by remember { mutableStateOf(keyStorage.getGeminiKey()) }
    var groqKey by remember { mutableStateOf(keyStorage.getGroqKey()) }

    var showElevenLabsKey by remember { mutableStateOf(false) }
    var showGeminiKey by remember { mutableStateOf(false) }
    var showGroqKey by remember { mutableStateOf(false) }

    var isSavedNotification by remember { mutableStateOf(false) }

    ClaymorphicCard(
        modifier = modifier.fillMaxWidth(),
        borderColor = NeonRed,
        glowColor = NeonRed,
        testTag = "connectors_card_main"
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
                            .background(NeonRed.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = "Connectors",
                            tint = NeonRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AI CONNECTORS MATRIX",
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "⚡", fontSize = 13.sp)
                        }
                        Text(
                            text = "Hardware EncryptedSharedPreferences (AES-256)",
                            color = ImperialGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(EmpireCardElevated, RoundedCornerShape(8.dp))
                        .border(1.dp, if (keyStorage.hasAnyKey()) AgentGreen else TextMuted, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (keyStorage.hasAnyKey()) "ACTIVE" else "UNLINKED",
                        color = if (keyStorage.hasAnyKey()) AgentGreen else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "Enter your API keys below to unlock ultra-fast Groq Llama 3.1, Gemini Flash, and ElevenLabs neural speech across all 11 agents.",
                color = TextMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            // Field 1: ElevenLabs API Key
            ConnectorInputField(
                label = "ElevenLabs API Key",
                value = elevenLabsKey,
                onValueChange = { elevenLabsKey = it },
                placeholder = "sk_... Neural voice clone",
                showKey = showElevenLabsKey,
                onToggleVisibility = { showElevenLabsKey = !showElevenLabsKey },
                isConfigured = elevenLabsKey.isNotBlank(),
                accentColor = NeonRed,
                testTag = "input_elevenlabs_key"
            )

            // Field 2: Gemini API Key
            ConnectorInputField(
                label = "Gemini API Key",
                value = geminiKey,
                onValueChange = { geminiKey = it },
                placeholder = "AIzaSy... Google Gemini 2.5 Flash",
                showKey = showGeminiKey,
                onToggleVisibility = { showGeminiKey = !showGeminiKey },
                isConfigured = geminiKey.isNotBlank(),
                accentColor = ImperialGold,
                testTag = "input_gemini_key"
            )

            // Field 3: Groq API Key
            ConnectorInputField(
                label = "Groq API Key",
                value = groqKey,
                onValueChange = { groqKey = it },
                placeholder = "gsk_... Llama 3.1 70B ultra-fast",
                showKey = showGroqKey,
                onToggleVisibility = { showGroqKey = !showGroqKey },
                isConfigured = groqKey.isNotBlank(),
                accentColor = AgentTeal,
                testTag = "input_groq_key"
            )

            // Save Button
            Button(
                onClick = {
                    keyStorage.setElevenLabsKey(elevenLabsKey)
                    keyStorage.setGeminiKey(geminiKey)
                    keyStorage.setGroqKey(groqKey)
                    isSavedNotification = true
                    Toast.makeText(context, "✅ Keys Encrypted & Saved into Keystore Vault!", Toast.LENGTH_SHORT).show()
                    onKeysUpdated?.invoke()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("save_connectors_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonRed,
                    contentColor = TextWhite
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Save",
                    modifier = Modifier.size(16.dp),
                    tint = TextWhite
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SAVE KEYS TO ENCRYPTED VAULT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            AnimatedVisibility(visible = isSavedNotification) {
                Text(
                    text = "✓ Vault updated: All 11 agents synchronized with new credentials.",
                    color = AgentGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ConnectorInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    showKey: Boolean,
    onToggleVisibility: () -> Unit,
    isConfigured: Boolean,
    accentColor: Color,
    testTag: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = TextWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (isConfigured) "● Connected" else "○ Not configured",
                color = if (isConfigured) AgentGreen else TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextMuted, fontSize = 12.sp) },
            singleLine = true,
            visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle visibility",
                        tint = if (value.isNotBlank()) accentColor else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = EmpireCardBorder,
                focusedContainerColor = EmpireCardBg,
                unfocusedContainerColor = EmpireCardBg,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite
            )
        )
    }
}
