package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ResumeScanResult
import com.example.model.SampleResumes
import com.example.service.ResumaxService
import com.example.service.SpeechService
import com.example.ui.components.ClaymorphicCard
import com.example.ui.components.OrbWidget
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResumaxScreen(
    resumaxService: ResumaxService,
    speechService: SpeechService,
    repository: com.example.data.EmpireFirestoreRepository? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val isSpeaking by speechService.isSpeaking.collectAsState()

    var resumeText by remember { mutableStateOf(SampleResumes.samples[0].rawText) }
    var targetJob by remember { mutableStateOf("Senior Mobile & AI Architect") }
    var scanResult by remember { mutableStateOf<ResumeScanResult?>(null) }
    var isAuditing by remember { mutableStateOf(false) }
    var showCoverLetter by remember { mutableStateOf(false) }

    // File picker for PDF / TXT / DOC
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val content = reader.readText()
                    if (content.isNotBlank()) {
                        resumeText = content
                        Toast.makeText(context, "Resume loaded from file!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Loaded file name reference. Ready to audit.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Run initial scan on load if null
    LaunchedEffect(Unit) {
        scanResult = resumaxService.analyzeResume(resumeText, targetJob)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(EmpireBlack)
            .statusBarsPadding()
            .testTag("resumax_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Top Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("resumax_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ImperialGold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "RESUMAX 👑",
                            color = TextWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(AgentOrange, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "CAREER AI",
                                color = EmpireBlack,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Text(
                        text = "Resume + ATS Score + Voice Assistant",
                        color = ImperialGold,
                        fontSize = 12.sp
                    )
                }

                // Voice assistant indicator
                IconButton(
                    onClick = {
                        scanResult?.let {
                            if (isSpeaking) speechService.stop() else speechService.speak(it.vocalScript)
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (isSpeaking) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Voice Assistant",
                        tint = if (isSpeaking) NeonRed else ImperialGold
                    )
                }
            }
        }

        // 2. Score Meter Card
        item {
            val result = scanResult
            ClaymorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = ImperialGold,
                glowColor = ImperialGold,
                borderWidth = 2.dp,
                elevation = 10.dp,
                testTag = "ats_score_card"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ATS READINESS SCORE",
                                color = ImperialGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = result?.candidateName ?: "Applicant",
                                color = TextWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Target: ${result?.targetJobTitle ?: targetJob}",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }

                        // Big ATS radial score circle
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(EmpireBlack)
                                .border(3.dp, ImperialGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${result?.overallScore ?: 85}",
                                    color = ImperialGold,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "/ 100",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Category Breakdown bars
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScoreBar(label = "Quantifiable Impact & Metrics", score = result?.impactScore ?: 22, max = 25, color = ImperialGold)
                        ScoreBar(label = "Action Verbs & Authority", score = result?.verbScore ?: 18, max = 20, color = NeonRed)
                        ScoreBar(label = "Target Skills & Keywords", score = result?.skillsScore ?: 26, max = 30, color = AgentGreen)
                        ScoreBar(label = "ATS Section Formatting", score = result?.formatScore ?: 22, max = 25, color = AgentCyan)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Voice Coach Button
                    Button(
                        onClick = {
                            result?.let {
                                if (isSpeaking) speechService.stop() else speechService.speak(it.vocalScript)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSpeaking) NeonRed else ImperialGold,
                            contentColor = EmpireBlack
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("resumax_speak_button")
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.RecordVoiceOver else Icons.Default.VolumeUp,
                            contentDescription = "TTS Voice",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSpeaking) "Speaking ATS Review (Tap to Stop)" else "Listen to RESUMAX Vocal Review",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. Missing Keywords & Match Chips
        item {
            val result = scanResult
            ClaymorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = EmpireCardBorder,
                testTag = "keywords_card"
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "CRITICAL MISSING KEYWORDS (ATS GAP)",
                        color = NeonRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (result?.missingKeywords ?: emptyList()).forEach { kw ->
                            Box(
                                modifier = Modifier
                                    .background(NeonRedDark, RoundedCornerShape(8.dp))
                                    .border(1.dp, NeonRed, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "+ $kw",
                                    color = NeonRedGlow,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "MATCHED SKILLS & STRENGTHS",
                        color = AgentGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        (result?.matchingKeywords ?: emptyList()).take(8).forEach { kw ->
                            Box(
                                modifier = Modifier
                                    .background(EmpireCardElevated, RoundedCornerShape(8.dp))
                                    .border(1.dp, AgentGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "✓ $kw",
                                    color = AgentGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Input Section: Upload Resume, Sample Presets, Target Job
        item {
            ClaymorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = EmpireCardBorder,
                testTag = "resume_input_card"
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "RESUME AUDIT BENCHMARK",
                        color = ImperialGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Target Job Title input
                    OutlinedTextField(
                        value = targetJob,
                        onValueChange = { targetJob = it },
                        label = { Text("Target Job Title", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ImperialGold,
                            unfocusedBorderColor = EmpireCardBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Upload PDF / File Button & Sample Selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { filePickerLauncher.launch("*/*") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmpireCardElevated,
                                contentColor = ImperialGold
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ImperialGold),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Upload File", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val sample = SampleResumes.samples.first()
                                resumeText = sample.rawText
                                targetJob = sample.jobTitle
                                scanResult = resumaxService.analyzeResume(resumeText, targetJob)
                                Toast.makeText(context, "Loaded Sample: ${sample.label}", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmpireCardElevated,
                                contentColor = TextWhite
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Load Sample", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Run Audit Action Button
                    Button(
                        onClick = {
                            isAuditing = true
                            scanResult = resumaxService.analyzeResume(resumeText, targetJob)
                            isAuditing = false
                            Toast.makeText(context, "Audit complete! Score updated.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonRed,
                            contentColor = TextWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("run_audit_button")
                    ) {
                        Text(
                            text = if (isAuditing) "Evaluating ATS Matrix..." else "Re-Calculate ATS Score",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 5. Generated Cover Letter Section
        item {
            val result = scanResult
            ClaymorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = ImperialGoldDark,
                testTag = "cover_letter_card"
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI TAILORED COVER LETTER",
                            color = ImperialGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        TextButton(
                            onClick = {
                                result?.generatedCoverLetter?.let {
                                    clipboardManager.setText(AnnotatedString(it))
                                    Toast.makeText(context, "Cover Letter Copied!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = ImperialGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Copy", color = ImperialGold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = result?.generatedCoverLetter ?: "Audit your resume to generate a tailored cover letter.",
                        color = TextOffWhite,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // 6. Memory History from resumax_memory.json
        item {
            val history = resumaxService.getMemoryHistory()
            if (history.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "RESUMAX MEMORY HISTORY (resumax_memory.json)",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    history.forEach { item ->
                        Text(
                            text = "• $item",
                            color = TextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun ScoreBar(
    label: String,
    score: Int,
    max: Int,
    color: Color
) {
    val progress = (score.toFloat() / max).coerceIn(0f, 1f)
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = TextOffWhite, fontSize = 12.sp)
            Text(text = "$score / $max", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(EmpireCardElevated)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}
