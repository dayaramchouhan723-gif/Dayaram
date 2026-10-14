package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.OrbWidget
import com.example.ui.theme.EmpireBlack
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.NeonRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

@Composable
fun SplashScreen(
    onContinue: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EmpireBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("splash_screen")
    ) {
        // Bottom atmospheric red glow
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            NeonRed.copy(alpha = 0.12f),
                            NeonRed.copy(alpha = 0.35f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Center red orbital orb with rings + particles + golden crown top
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                OrbWidget(
                    size = 210.dp,
                    baseColor = NeonRed,
                    secondaryColor = ImperialGold,
                    showCrown = true,
                    showParticles = true,
                    ringCount = 5,
                    orbType = "Neon"
                )

                Spacer(modifier = Modifier.height(36.dp))

                // Title: CHOUHAN EMPIRE 👑
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "CHOUHAN EMPIRE",
                        color = TextWhite,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "👑",
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Subtitle: "Intelligence that understands. Empire that cares." where cares is red
                val subtitleText = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = TextMuted, fontSize = 15.sp)) {
                        append("Intelligence that understands. Empire that ")
                    }
                    withStyle(style = SpanStyle(color = NeonRed, fontSize = 15.sp, fontWeight = FontWeight.Bold)) {
                        append("cares.")
                    }
                }

                Text(
                    text = subtitleText,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Bottom CTA button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onContinue,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonRed,
                        contentColor = TextWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("splash_enter_button")
                ) {
                    Text(
                        text = "Enter Empire 2.2.10",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Proceed",
                        tint = ImperialGold
                    )
                }
            }
        }
    }
}
