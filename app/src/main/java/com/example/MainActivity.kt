package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.example.data.EmpireFirestoreRepository
import com.example.model.AgentProfile
import com.example.service.AgentManager
import com.example.service.AiConnectorService
import com.example.service.AuraManager
import com.example.service.KeyStorageManager
import com.example.service.ResumaxService
import com.example.service.SpeechService
import com.example.ui.components.ClaymorphicCard
import com.example.ui.components.ConnectorsCard
import com.example.ui.components.OrbWidget
import com.example.ui.components.VoiceCloneStudioCard
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class AppScreen {
    SPLASH,
    ONBOARDING,
    HOME,
    CHAT,
    DISCOVER, // Multi-Agent Hub
    RESUMAX,
    VISUAL_LENS,
    AURA_CONTROL,
    ORB_CUSTOMIZATION,
    CONNECTORS,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    private lateinit var speechService: SpeechService
    private lateinit var resumaxService: ResumaxService
    private lateinit var auraManager: AuraManager
    private lateinit var agentManager: AgentManager
    private lateinit var keyStorageManager: KeyStorageManager
    private lateinit var aiConnectorService: AiConnectorService
    private var repository: EmpireFirestoreRepository? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        keyStorageManager = KeyStorageManager(applicationContext)
        aiConnectorService = AiConnectorService(applicationContext, keyStorageManager)
        speechService = SpeechService(applicationContext)
        resumaxService = ResumaxService(applicationContext)
        auraManager = AuraManager(applicationContext)
        agentManager = AgentManager(applicationContext, speechService, aiConnectorService)

        setContent {
            MyApplicationTheme {
                var currentUser by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser) }

                DisposableEffect(Unit) {
                    val authInstance = FirebaseAuth.getInstance()
                    val listener = FirebaseAuth.AuthStateListener { auth ->
                        currentUser = auth.currentUser
                        if (auth.currentUser != null) {
                            try {
                                repository = EmpireFirestoreRepository(this@MainActivity)
                            } catch (e: Exception) {
                                // Repository will initialize once database is available
                            }
                        }
                    }
                    authInstance.addAuthStateListener(listener)
                    onDispose {
                        authInstance.removeAuthStateListener(listener)
                    }
                }

                if (currentUser == null) {
                    AuthScreen(
                        onAuthSuccess = {
                            currentUser = FirebaseAuth.getInstance().currentUser
                            try {
                                repository = EmpireFirestoreRepository(this@MainActivity)
                            } catch (e: Exception) {
                                // Ignore
                            }
                        }
                    )
                } else {
                    EmpireApp(
                        speechService = speechService,
                        resumaxService = resumaxService,
                        auraManager = auraManager,
                        agentManager = agentManager,
                        keyStorageManager = keyStorageManager,
                        aiConnectorService = aiConnectorService,
                        repository = repository,
                        userEmail = currentUser?.email ?: "CHOUHAN STAR",
                        onSignOut = {
                            val credentialManager = CredentialManager.create(this@MainActivity)
                            FirebaseAuth.getInstance().signOut()
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                                } catch (e: Exception) {
                                    // Handled
                                }
                                currentUser = null
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechService.shutdown()
        aiConnectorService.release()
    }
}

@Composable
fun EmpireApp(
    speechService: SpeechService,
    resumaxService: ResumaxService,
    auraManager: AuraManager,
    agentManager: AgentManager,
    keyStorageManager: KeyStorageManager,
    aiConnectorService: AiConnectorService,
    repository: EmpireFirestoreRepository?,
    userEmail: String,
    onSignOut: () -> Unit
) {
    var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
    var chatInitialPrompt by remember { mutableStateOf<String?>(null) }
    var showVoiceModal by remember { mutableStateOf(false) }

    val activeAgent by agentManager.activeAgent.collectAsState()
    val isSpeaking by speechService.isSpeaking.collectAsState()
    val auraBorderMode by auraManager.auraBorderMode.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // TTS Greeting on app start: "Jai Mata Di Boss - 11 agents online" using ElevenLabs if key present
    LaunchedEffect(Unit) {
        val greetingText = "Jai Mata Di Boss - 11 agents online"
        coroutineScope.launch {
            aiConnectorService.speakWithElevenLabs(greetingText) { fallback ->
                speechService.speak(fallback)
            }
        }
    }

    // Screen stack management with BackHandler
    if (currentScreen != AppScreen.SPLASH && currentScreen != AppScreen.HOME) {
        BackHandler {
            currentScreen = AppScreen.HOME
        }
    }

    val isMainTab = currentScreen in listOf(AppScreen.HOME, AppScreen.CHAT, AppScreen.DISCOVER, AppScreen.SETTINGS)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EmpireBlack)
            .then(
                if (auraBorderMode) {
                    Modifier.border(1.5.dp, activeAgent.orbColor.copy(alpha = 0.5f))
                } else Modifier
            )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = EmpireBlack,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (isMainTab) {
                    EmpireBottomNavBar(
                        currentScreen = currentScreen,
                        activeAgent = activeAgent,
                        isSpeaking = isSpeaking,
                        onSelectTab = { screen ->
                            auraManager.triggerHaptic()
                            currentScreen = screen
                        },
                        onCenterOrbClick = {
                            auraManager.triggerHaptic(force = true)
                            showVoiceModal = true
                        }
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (isMainTab) 78.dp else 0.dp)
            ) {
                when (currentScreen) {
                    AppScreen.SPLASH -> {
                        SplashScreen(
                            onContinue = { currentScreen = AppScreen.ONBOARDING }
                        )
                    }
                    AppScreen.ONBOARDING -> {
                        OnboardingScreen(
                            onGetStarted = { currentScreen = AppScreen.HOME }
                        )
                    }
                    AppScreen.HOME -> {
                        HomeScreen(
                            activeAgent = activeAgent,
                            keyStorage = keyStorageManager,
                            speechService = speechService,
                            aiConnectorService = aiConnectorService,
                            onNavigateToChat = { prompt ->
                                chatInitialPrompt = prompt
                                currentScreen = AppScreen.CHAT
                            },
                            onNavigateToVisualLens = { currentScreen = AppScreen.VISUAL_LENS },
                            onNavigateToAuraControl = { currentScreen = AppScreen.AURA_CONTROL },
                            onNavigateToAgentHub = { currentScreen = AppScreen.DISCOVER },
                            onNavigateToResumax = { currentScreen = AppScreen.RESUMAX },
                            onStartVoiceMode = {
                                auraManager.triggerHaptic()
                                showVoiceModal = true
                            }
                        )
                    }
                    AppScreen.CHAT -> {
                        ChatScreen(
                            agentManager = agentManager,
                            speechService = speechService,
                            onBack = { currentScreen = AppScreen.HOME },
                            initialPrompt = chatInitialPrompt
                        )
                    }
                    AppScreen.DISCOVER -> {
                        AgentHubScreen(
                            currentAgent = activeAgent,
                            onSelectAgent = { agent ->
                                agentManager.switchAgent(agent)
                                auraManager.triggerHaptic()
                                coroutineScope.launch {
                                    try {
                                        repository?.savePreferences(activeAgentId = agent.id)
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                }
                            },
                            onOpenResumax = { currentScreen = AppScreen.RESUMAX }
                        )
                    }
                    AppScreen.RESUMAX -> {
                        ResumaxScreen(
                            resumaxService = resumaxService,
                            speechService = speechService,
                            repository = repository,
                            onBack = { currentScreen = AppScreen.HOME }
                        )
                    }
                    AppScreen.VISUAL_LENS -> {
                        VisualLensScreen(
                            onBack = { currentScreen = AppScreen.HOME },
                            onAnalyzeCompleted = { resultText ->
                                chatInitialPrompt = "Analyze this visual data: $resultText"
                                currentScreen = AppScreen.CHAT
                            }
                        )
                    }
                    AppScreen.AURA_CONTROL -> {
                        AuraControlScreen(
                            auraManager = auraManager,
                            onBack = { currentScreen = AppScreen.HOME }
                        )
                    }
                    AppScreen.ORB_CUSTOMIZATION -> {
                        OrbCustomizationScreen(
                            auraManager = auraManager,
                            onBack = { currentScreen = AppScreen.SETTINGS }
                        )
                    }
                    AppScreen.CONNECTORS -> {
                        ConnectorsScreen(
                            onBack = { currentScreen = AppScreen.SETTINGS }
                        )
                    }
                    AppScreen.SETTINGS -> {
                        SettingsHubScreen(
                            activeAgent = activeAgent,
                            userEmail = userEmail,
                            onNavigateToAura = { currentScreen = AppScreen.AURA_CONTROL },
                            onNavigateToOrb = { currentScreen = AppScreen.ORB_CUSTOMIZATION },
                            onNavigateToConnectors = { currentScreen = AppScreen.CONNECTORS },
                            onNavigateToResumax = { currentScreen = AppScreen.RESUMAX },
                            onSignOut = onSignOut
                        )
                    }
                }
            }
        }

        // Voice Mode Overlay Modal
        if (showVoiceModal) {
            VoiceInteractionModal(
                activeAgent = activeAgent,
                speechService = speechService,
                onDismiss = {
                    speechService.stop()
                    showVoiceModal = false
                },
                onSendPrompt = { prompt ->
                    showVoiceModal = false
                    chatInitialPrompt = prompt
                    currentScreen = AppScreen.CHAT
                }
            )
        }
    }
}

@Composable
fun EmpireBottomNavBar(
    currentScreen: AppScreen,
    activeAgent: AgentProfile,
    isSpeaking: Boolean,
    onSelectTab: (AppScreen) -> Unit,
    onCenterOrbClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .background(Color(0xFF0A0A0E))
            .border(width = 1.dp, color = EmpireCardBorder)
            .height(68.dp)
            .testTag("empire_bottom_nav"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Home
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentScreen == AppScreen.HOME,
                activeColor = NeonRed,
                onClick = { onSelectTab(AppScreen.HOME) }
            )

            // 2. Chat
            BottomNavItem(
                icon = Icons.Default.ChatBubble,
                label = "Chat",
                isSelected = currentScreen == AppScreen.CHAT,
                activeColor = activeAgent.orbColor,
                onClick = { onSelectTab(AppScreen.CHAT) }
            )

            // 3. Center Orb (Quick AI Action)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .offset(y = (-10).dp)
                    .clip(CircleShape)
                    .background(EmpireBlack)
                    .border(2.dp, activeAgent.orbColor, CircleShape)
                    .clickable { onCenterOrbClick() }
                    .testTag("center_orb_nav_button"),
                contentAlignment = Alignment.Center
            ) {
                OrbWidget(
                    size = 50.dp,
                    baseColor = activeAgent.orbColor,
                    secondaryColor = ImperialGold,
                    showCrown = false,
                    showParticles = false,
                    ringCount = 2,
                    orbType = "Neon",
                    isSpeaking = isSpeaking
                )
            }

            // 4. Discover (Agent Hub)
            BottomNavItem(
                icon = Icons.Default.Explore,
                label = "Discover",
                isSelected = currentScreen == AppScreen.DISCOVER,
                activeColor = ImperialGold,
                onClick = { onSelectTab(AppScreen.DISCOVER) }
            )

            // 5. Settings / Control
            BottomNavItem(
                icon = Icons.Default.Tune,
                label = "Settings",
                isSelected = currentScreen == AppScreen.SETTINGS,
                activeColor = NeonRed,
                onClick = { onSelectTab(AppScreen.SETTINGS) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) activeColor else TextMuted,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isSelected) activeColor else TextMuted,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun SettingsHubScreen(
    activeAgent: AgentProfile,
    userEmail: String,
    onNavigateToAura: () -> Unit,
    onNavigateToOrb: () -> Unit,
    onNavigateToConnectors: () -> Unit,
    onNavigateToResumax: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EmpireBlack)
            .statusBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "EMPIRE SETTINGS",
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "👑", fontSize = 18.sp)
        }
        Text(
            text = "Personalize System Identity, Connectors & Audio Matrix",
            color = ImperialGold,
            fontSize = 12.sp
        )

        // Cloud User Profile Card
        ClaymorphicCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = ImperialGold,
            glowColor = ImperialGold
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GOOGLE CLOUD SYNC",
                        color = ImperialGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = userEmail,
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Firestore: Connected & Synchronized",
                        color = AgentGreen,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = onSignOut,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonRedDark,
                        contentColor = NeonRedGlow
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonRed),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Sign Out", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        ClaymorphicCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = NeonRed,
            onClick = onNavigateToAura,
            testTag = "settings_aura_card"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Aura Control", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Visual Identity, Crimson Aura & Haptics", color = TextMuted, fontSize = 12.sp)
                }
                Icon(Icons.Default.Tune, contentDescription = null, tint = NeonRed)
            }
        }

        ClaymorphicCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = AgentGreen,
            onClick = onNavigateToOrb,
            testTag = "settings_orb_card"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Orb Customization", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Green+Blue neon rings, plasma types & size slider", color = TextMuted, fontSize = 12.sp)
                }
                Icon(Icons.Default.TrackChanges, contentDescription = null, tint = AgentGreen)
            }
        }

        ClaymorphicCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = ImperialGold,
            onClick = onNavigateToConnectors,
            testTag = "settings_connectors_card"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "AI Connectors", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Gemini Flash, Groq, Claude, OpenAI, ElevenLabs", color = TextMuted, fontSize = 12.sp)
                }
                Icon(Icons.Default.Hub, contentDescription = null, tint = ImperialGold)
            }
        }

        ClaymorphicCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = AgentOrange,
            onClick = onNavigateToResumax,
            testTag = "settings_resumax_card"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "RESUMAX Career Engine", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Audit resume, ATS score 85/100, cover letters", color = TextMuted, fontSize = 12.sp)
                }
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AgentOrange)
            }
        }
    }
}

@Composable
fun VoiceInteractionModal(
    activeAgent: AgentProfile,
    speechService: SpeechService,
    onDismiss: () -> Unit,
    onSendPrompt: (String) -> Unit
) {
    val isSpeaking by speechService.isSpeaking.collectAsState()

    LaunchedEffect(Unit) {
        speechService.speak("Empire Voice Mode online with ${activeAgent.name}. Listening.")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        ClaymorphicCard(
            modifier = Modifier
                .width(320.dp)
                .clickable(enabled = false) {},
            borderColor = activeAgent.orbColor,
            glowColor = activeAgent.orbColor,
            elevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "VOICE MODE",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "👑", fontSize = 14.sp)
                }
                Text(
                    text = "Connected: ${activeAgent.name}",
                    color = activeAgent.orbColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
                )

                OrbWidget(
                    size = 130.dp,
                    baseColor = activeAgent.orbColor,
                    secondaryColor = ImperialGold,
                    showCrown = true,
                    showParticles = true,
                    ringCount = 4,
                    orbType = "Neon",
                    isSpeaking = isSpeaking
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (isSpeaking) "Speaking response..." else "Talk to EMPIRE - Always on",
                    color = TextOffWhite,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onSendPrompt("Hello ${activeAgent.name}, give me an empire status briefing.")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = activeAgent.orbColor,
                            contentColor = EmpireBlack
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Briefing", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmpireCardElevated,
                            contentColor = TextWhite
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
