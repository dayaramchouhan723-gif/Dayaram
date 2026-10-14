package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.EmpireCardBg
import com.example.ui.theme.EmpireCardBorder
import com.example.ui.theme.EmpireCardElevated
import com.example.ui.theme.NeonRed

@Composable
fun ClaymorphicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    borderColor: Color = EmpireCardBorder,
    borderWidth: Dp = 1.dp,
    glowColor: Color = Color.Transparent,
    elevation: Dp = 6.dp,
    testTag: String = "clay_card",
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    val cardModifier = modifier
        .testTag(testTag)
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = glowColor.copy(alpha = 0.35f),
            spotColor = glowColor.copy(alpha = 0.5f)
        )
        .clip(shape)
        .then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        )

    Card(
        modifier = cardModifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(
            borderWidth,
            Brush.linearGradient(
                colors = listOf(
                    borderColor.copy(alpha = 0.85f),
                    borderColor.copy(alpha = 0.25f),
                    borderColor.copy(alpha = 0.6f)
                )
            )
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            EmpireCardElevated,
                            EmpireCardBg,
                            Color(0xFF0A0A0D)
                        )
                    )
                )
        ) {
            content()
        }
    }
}
