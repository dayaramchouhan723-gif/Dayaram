package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.NeonRed
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OrbWidget(
    modifier: Modifier = Modifier,
    baseColor: Color = NeonRed,
    secondaryColor: Color = ImperialGold,
    size: Dp = 160.dp,
    showCrown: Boolean = true,
    showParticles: Boolean = true,
    ringCount: Int = 4,
    orbType: String = "Neon", // Classic, Energy, Neon
    isSpeaking: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")

    // Pulse animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = if (isSpeaking) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isSpeaking) 600 else 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Orbital rotation
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbital_rotation"
    )

    // Outer glow alpha
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ) else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val baseRadius = (size.toPx() / 2.8f) * pulseScale

            // 1. Outermost Ambient Atmospheric Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        baseColor.copy(alpha = 0.45f * glowAlpha),
                        baseColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.6f
                ),
                radius = baseRadius * 1.6f,
                center = center
            )

            // 2. Concentric Neon Orbital Rings
            for (i in 1..ringCount) {
                val ringRadius = baseRadius * (0.8f + (i * 0.18f))
                val ringAlpha = (0.7f - (i * 0.12f)).coerceIn(0.15f, 0.85f)
                val strokeWidth = if (i == 1) 2.5f else 1.5f

                // Elliptical tilt or circular orbit
                drawCircle(
                    color = if (i % 2 == 0) secondaryColor.copy(alpha = ringAlpha) else baseColor.copy(alpha = ringAlpha),
                    radius = ringRadius,
                    center = center,
                    style = Stroke(
                        width = strokeWidth,
                        // Dash or solid
                    )
                )

                // Orbital satellites / particles
                if (showParticles) {
                    val angleRad = Math.toRadians((rotationAngle * (if (i % 2 == 0) 1 else -1) + (i * 72)).toDouble())
                    val particleX = center.x + (ringRadius * cos(angleRad)).toFloat()
                    val particleY = center.y + (ringRadius * sin(angleRad)).toFloat()

                    drawCircle(
                        color = if (i % 2 == 0) secondaryColor else baseColor,
                        radius = 3.5f,
                        center = Offset(particleX, particleY)
                    )
                }
            }

            // 3. Central Core Orb Gradient
            val coreGradient = when (orbType) {
                "Energy" -> Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        secondaryColor,
                        baseColor,
                        Color.Black
                    ),
                    center = center.copy(y = center.y - baseRadius * 0.25f),
                    radius = baseRadius
                )
                "Classic" -> Brush.radialGradient(
                    colors = listOf(
                        baseColor.copy(alpha = 0.9f),
                        baseColor.copy(alpha = 0.6f),
                        Color.Black
                    ),
                    center = center,
                    radius = baseRadius
                )
                else -> Brush.radialGradient( // Neon
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        baseColor,
                        secondaryColor.copy(alpha = 0.8f),
                        baseColor.copy(alpha = 0.2f)
                    ),
                    center = center.copy(x = center.x - baseRadius * 0.2f, y = center.y - baseRadius * 0.2f),
                    radius = baseRadius
                )
            }

            drawCircle(
                brush = coreGradient,
                radius = baseRadius * 0.65f,
                center = center
            )

            // Inner high-specular glossy reflection
            drawCircle(
                color = Color.White.copy(alpha = 0.35f),
                radius = baseRadius * 0.22f,
                center = center.copy(x = center.x - baseRadius * 0.25f, y = center.y - baseRadius * 0.25f)
            )

            // Voice audio wave lines if speaking
            if (isSpeaking) {
                val waveHeight = baseRadius * 0.3f
                for (step in -3..3) {
                    val x = center.x + (step * 14f)
                    val factor = 1f - (kotlin.math.abs(step) * 0.2f)
                    val h = waveHeight * factor * pulseScale
                    drawLine(
                        color = secondaryColor,
                        start = Offset(x, center.y - h),
                        end = Offset(x, center.y + h),
                        strokeWidth = 3f
                    )
                }
            }
        }

        // 4. Golden Imperial Crown on top if requested
        if (showCrown) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-4).dp)
            ) {
                Text(
                    text = "👑",
                    fontSize = (size.value * 0.22f).sp
                )
            }
        }
    }
}
