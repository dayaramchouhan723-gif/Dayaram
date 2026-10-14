package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectorItem
import com.example.service.KeyStorageManager
import com.example.ui.components.ClaymorphicCard
import com.example.ui.components.ConnectorsCard
import com.example.ui.theme.*

@Composable
fun ConnectorsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val keyStorage = remember { KeyStorageManager(context) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // All, Connected, Not Connected
    var keyDialogOpen by remember { mutableStateOf(false) }
    var selectedConnectorForDialog by remember { mutableStateOf<ConnectorItem?>(null) }
    var enteredKey by remember { mutableStateOf("") }

    // Connectors list initialized with persistent encrypted keys
    var connectors by remember {
        mutableStateOf(
            listOf(
                ConnectorItem("gemini", "Gemini", "Gemini Flash - Google AI", isConnected = keyStorage.getGeminiKey().isNotBlank(), category = "Core AI"),
                ConnectorItem("groq", "Groq", "Ultra fast Llama 3.1 70B", isConnected = keyStorage.getGroqKey().isNotBlank(), category = "Core AI"),
                ConnectorItem("openai", "OpenAI GPT", "GPT-4o & Reasoning Engine", isConnected = false, category = "Core AI"),
                ConnectorItem("claude", "Claude Anthropic", "Claude 3.5 Sonnet Artifacts", isConnected = false, category = "Core AI"),
                ConnectorItem("perplexity", "Perplexity", "Real-time Live Citations & Web", isConnected = false, category = "Web Search"),
                ConnectorItem("tavily", "Tavily", "Deep Web AI Crawler", isConnected = false, category = "Web Search"),
                ConnectorItem("elevenlabs", "ElevenLabs", "Hyper-realistic Neural Voice", isConnected = keyStorage.getElevenLabsKey().isNotBlank(), category = "Audio & Voice"),
                ConnectorItem("local_engine", "Local Neural Core", "Offline On-Device Inference", isConnected = true, category = "Device Core", badge = "Built-in Free")
            )
        )
    }

    val connectedCount = connectors.count { it.isConnected }
    val totalCount = 20

    val filteredList = connectors.filter { item ->
        val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) || item.subtitle.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Connected" -> item.isConnected
            "Not Connected" -> !item.isConnected
            else -> true
        }
        matchesSearch && matchesFilter
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(EmpireBlack)
            .statusBarsPadding()
            .testTag("connectors_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Title + 0 of 20 connected
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
                        text = "CONNECTORS",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$connectedCount of $totalCount connected",
                        color = if (connectedCount > 0) ImperialGold else NeonRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. Search bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search 20 AI connectors...", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ImperialGold) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonRed,
                    unfocusedBorderColor = EmpireCardBorder,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                )
            )
        }

        // 3. Filter Pills: All / Connected / Not Connected (All active red)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("All", "Connected", "Not Connected").forEach { filter ->
                    val isActive = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isActive) NeonRed else EmpireCardElevated)
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter,
                            color = if (isActive) TextWhite else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 3B. Quick Key Config Matrix
        item {
            ConnectorsCard(
                keyStorage = keyStorage,
                onKeysUpdated = {
                    connectors = connectors.map { c ->
                        when (c.id) {
                            "gemini" -> c.copy(isConnected = keyStorage.getGeminiKey().isNotBlank())
                            "groq" -> c.copy(isConnected = keyStorage.getGroqKey().isNotBlank())
                            "elevenlabs" -> c.copy(isConnected = keyStorage.getElevenLabsKey().isNotBlank())
                            else -> c
                        }
                    }
                }
            )
        }

        // 4. Connectors List
        items(filteredList, key = { it.id }) { item ->
            ClaymorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (item.isConnected) ImperialGold else EmpireCardBorder,
                onClick = {
                    selectedConnectorForDialog = item
                    keyDialogOpen = true
                },
                testTag = "connector_item_${item.id}"
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.name,
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(EmpireCardElevated, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.badge,
                                    color = ImperialGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Text(
                            text = item.subtitle,
                            color = TextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.isConnected) {
                            Text(
                                text = "Connected",
                                color = AgentGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "Not connected",
                                color = NeonRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Configure",
                            tint = ImperialGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Add keys dialog
    if (keyDialogOpen && selectedConnectorForDialog != null) {
        val connector = selectedConnectorForDialog!!
        AlertDialog(
            onDismissRequest = { keyDialogOpen = false },
            title = {
                Text(
                    text = "Configure ${connector.name}",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "CHOUHAN EMPIRE includes lifetime free mock intelligence mode. You can also provide an API key or use the built-in Secrets panel.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = enteredKey,
                        onValueChange = { enteredKey = it },
                        placeholder = { Text("Enter API key or leave blank for Free", color = TextMuted, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonRed,
                            unfocusedBorderColor = EmpireCardBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (enteredKey.isNotBlank()) {
                            when (connector.id) {
                                "gemini" -> keyStorage.setGeminiKey(enteredKey)
                                "groq" -> keyStorage.setGroqKey(enteredKey)
                                "elevenlabs" -> keyStorage.setElevenLabsKey(enteredKey)
                            }
                        }
                        connectors = connectors.map {
                            if (it.id == connector.id) it.copy(isConnected = true) else it
                        }
                        keyDialogOpen = false
                        Toast.makeText(context, "${connector.name} encrypted & connected successfully!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonRed)
                ) {
                    Text("Connect Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { keyDialogOpen = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = EmpireCardBg
        )
    }
}
