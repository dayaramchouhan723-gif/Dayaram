package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ClaymorphicCard
import com.example.ui.theme.*

@Composable
fun VisualLensScreen(
    onBack: () -> Unit,
    onAnalyzeCompleted: (String) -> Unit
) {
    var isScanning by remember { mutableStateOf(false) }
    var scanReport by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "hud_scan")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_line"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EmpireBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("visual_lens_screen"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = ImperialGold
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "VISUAL LENS 2.2.10",
                    color = TextWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Neural Optical Recognition Active",
                    color = ImperialGold,
                    fontSize = 11.sp
                )
            }

            IconButton(onClick = { /* Toggle flashlight */ }) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Flash",
                    tint = ImperialGold
                )
            }
        }

        // 2. Center Viewfinder HUD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(EmpireCardElevated)
                .border(2.dp, NeonRed, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Reticle corners
                val cornerLen = 40f
                // Top Left
                drawLine(NeonRed, Offset(20f, 20f), Offset(20f + cornerLen, 20f), strokeWidth = 5f)
                drawLine(NeonRed, Offset(20f, 20f), Offset(20f, 20f + cornerLen), strokeWidth = 5f)
                // Top Right
                drawLine(NeonRed, Offset(w - 20f, 20f), Offset(w - 20f - cornerLen, 20f), strokeWidth = 5f)
                drawLine(NeonRed, Offset(w - 20f, 20f), Offset(w - 20f, 20f + cornerLen), strokeWidth = 5f)
                // Bottom Left
                drawLine(NeonRed, Offset(20f, h - 20f), Offset(20f + cornerLen, h - 20f), strokeWidth = 5f)
                drawLine(NeonRed, Offset(20f, h - 20f), Offset(20f, h - 20f - cornerLen), strokeWidth = 5f)
                // Bottom Right
                drawLine(NeonRed, Offset(w - 20f, h - 20f), Offset(w - 20f - cornerLen, h - 20f), strokeWidth = 5f)
                drawLine(NeonRed, Offset(w - 20f, h - 20f), Offset(w - 20f, h - 20f - cornerLen), strokeWidth = 5f)

                // Laser scan line
                val currentY = h * scanLineY
                drawLine(
                    color = ImperialGold,
                    start = Offset(24f, currentY),
                    end = Offset(w - 24f, currentY),
                    strokeWidth = 3f
                )

                // Central targeting reticle
                drawCircle(
                    color = ImperialGold.copy(alpha = 0.5f),
                    radius = 35f,
                    center = Offset(w / 2f, h / 2f),
                    style = Stroke(2f)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CenterFocusStrong,
                    contentDescription = null,
                    tint = ImperialGold,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isScanning) "ANALYZING SPECTRUM..." else "POINT AT OBJECT OR RESUME",
                    color = TextWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // 3. Bottom Controls & Result Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (scanReport != null) {
                ClaymorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = ImperialGold
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "OPTICAL INTELLIGENCE VERDICT",
                            color = ImperialGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = scanReport!!,
                            color = TextWhite,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    isScanning = true
                    scanReport = "CHOUHAN EMPIRE Visual Lens: Target parsed as High-Frequency Tech Infrastructure. 99.4% confidence score. Synchronized with active Agent."
                    isScanning = false
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonRed,
                    contentColor = TextWhite
                ),
                shape = CircleShape,
                modifier = Modifier
                    .size(76.dp)
                    .border(3.dp, ImperialGold, CircleShape)
                    .testTag("shutter_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Capture",
                    tint = TextWhite,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap to Capture & Analyze",
                color = TextMuted,
                fontSize = 12.sp
            )
        }
    }
}
