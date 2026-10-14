package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AgentProfile
import com.example.model.AgentProfiles
import com.example.ui.components.ClaymorphicCard
import com.example.ui.components.OrbWidget
import com.example.ui.theme.*

@Composable
fun AgentHubScreen(
    currentAgent: AgentProfile,
    onSelectAgent: (AgentProfile) -> Unit,
    onOpenResumax: () -> Unit
) {
    val allAgents = AgentProfiles.allAgents
    val resumaxAgent = allAgents.first { it.id == "resumax" }
    val otherAgents = allAgents.filter { it.id != "resumax" }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .background(EmpireBlack)
            .statusBarsPadding()
            .testTag("agent_hub_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header Span: "CHOUHAN EMPIRE 👑 - 11 Agents in 1 App - NEW: Resume ATS Agent Added"
        item(span = { GridItemSpan(2) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CHOUHAN EMPIRE",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "👑", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "11 Agents in 1 App — NEW: Resume ATS Agent Added",
                    color = ImperialGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap any agent to synchronize home orb, voice personality & tools.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // 2. BIGGEST CARD: RESUMAX with Gold Glow (Full width 2 columns span)
        item(span = { GridItemSpan(2) }) {
            val isSelected = currentAgent.id == resumaxAgent.id
            ClaymorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (isSelected) ImperialGold else ImperialGoldDark,
                glowColor = ImperialGold,
                borderWidth = if (isSelected) 2.dp else 1.5.dp,
                elevation = 12.dp,
                onClick = {
                    onSelectAgent(resumaxAgent)
                    onOpenResumax()
                },
                testTag = "resumax_featured_card"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Badge & Title
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(ImperialGold, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "NEW CAREER",
                                        color = EmpireBlack,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(NeonRedDark, RoundedCornerShape(8.dp))
                                        .border(1.dp, NeonRed, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "85/100 ATS SCORE",
                                        color = ImperialGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "RESUMAX",
                                color = TextWhite,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Resume + ATS Score + Voice Assistant",
                                color = ImperialGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Right: Big Golden Orange Orb with Document Check icon
                        Box(contentAlignment = Alignment.Center) {
                            OrbWidget(
                                size = 95.dp,
                                baseColor = AgentOrange,
                                secondaryColor = ImperialGold,
                                showCrown = true,
                                showParticles = true,
                                ringCount = 4,
                                orbType = "Neon"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Tools: Resume Parser • ATS Checker • Cover Letter • Job Match • Memory: resumax_memory.json",
                        color = TextOffWhite,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onOpenResumax,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ImperialGold,
                                contentColor = EmpireBlack
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "ATS Audit",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Audit My Resume Now",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isSelected) {
                            Text(
                                text = "● ACTIVE AGENT",
                                color = ImperialGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 3. Grid for the other 10 Agents (2 columns)
        items(otherAgents, key = { it.id }) { agent ->
            val isSelected = currentAgent.id == agent.id
            ClaymorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (isSelected) agent.orbColor else EmpireCardBorder,
                glowColor = if (isSelected) agent.orbColor else Color.Transparent,
                borderWidth = if (isSelected) 2.dp else 1.dp,
                elevation = if (isSelected) 8.dp else 4.dp,
                onClick = { onSelectAgent(agent) },
                testTag = "agent_card_${agent.id}"
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Agent Orb
                    OrbWidget(
                        size = 64.dp,
                        baseColor = agent.orbColor,
                        secondaryColor = agent.secondaryColor,
                        showCrown = false,
                        showParticles = false,
                        ringCount = 2,
                        orbType = "Neon"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = agent.name,
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = agent.role,
                        color = agent.orbColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = agent.description,
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Badge (e.g. Tools 8, Always Listening, 4 AI Core)
                    if (agent.badge != null) {
                        Box(
                            modifier = Modifier
                                .background(EmpireCardElevated, RoundedCornerShape(6.dp))
                                .border(1.dp, agent.orbColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = agent.badge,
                                color = TextOffWhite,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (isSelected) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "● ACTIVE",
                            color = agent.orbColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item(span = { GridItemSpan(2) }) {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
