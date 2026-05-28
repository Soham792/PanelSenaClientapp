package com.panelsena.client

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.panelsena.client.core.theme.CardCoral
import com.panelsena.client.core.theme.CardPink
import com.panelsena.client.core.theme.CardPurple
import com.panelsena.client.core.theme.CardSky
import com.panelsena.client.core.theme.CardYellow
import com.panelsena.client.core.theme.PanelSenaClientTheme
import com.panelsena.client.core.utils.ClientIdManager
import com.panelsena.client.ui.DisplayViewModel
import com.panelsena.client.ui.auth.AuthScreen
import com.panelsena.client.ui.components.CustomFloatingNavBar
import com.panelsena.client.ui.components.Premium3DIllustration
import com.panelsena.client.ui.home.HomeScreen
import com.panelsena.client.ui.info.DisplayInfoScreen
import com.panelsena.client.ui.schedule.ScheduleScreen
import com.panelsena.client.ui.splash.SplashScreen
import com.panelsena.client.ui.player.FullscreenPlayerScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var clientIdManager: ClientIdManager

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    private val viewModel: DisplayViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Keep display awake initially
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        enableEdgeToEdge()
        
        setContent {
            PanelSenaClientTheme {
                var currentScreen by remember { mutableStateOf("splash") } // "splash" | "auth" | "main" | "player"
                var selectedTab by remember { mutableIntStateOf(0) } // 0: Home, 1: Display, 2: Schedule, 3: Info
                var playerStartIndex by remember { mutableIntStateOf(0) }
                var showProfileSheet by remember { mutableStateOf(false) }
                
                // Dynamic Keep Awake Management
                var keepAwake by remember { mutableStateOf(true) }
                var offlineSync by remember { mutableStateOf(false) }

                LaunchedEffect(keepAwake) {
                    if (keepAwake) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }

                val isOnline by viewModel.isOnline.collectAsState()
                val displayContent by viewModel.displayContent.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentScreen == "main") {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                CustomFloatingNavBar(
                                    selectedTab = selectedTab,
                                    onTabSelected = { selectedTab = it },
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFF7F6F2))
                    ) {
                        // Display full-screen offline interruption if network is lost
                        if (!isOnline) {
                            OfflineOverlay()
                        } else {
                            // Primary Screen Switcher
                            when (currentScreen) {
                                "splash" -> {
                                    SplashScreen(
                                        clientIdManager = clientIdManager,
                                        onSplashComplete = {
                                            currentScreen = if (firebaseAuth.currentUser != null) "main" else "auth"
                                        }
                                    )
                                }
                                "auth" -> {
                                    AuthScreen(
                                        firebaseAuth = firebaseAuth,
                                        onSignInSuccess = {
                                            currentScreen = "main"
                                        }
                                    )
                                }
                                "main" -> {
                                    when (selectedTab) {
                                        0 -> HomeScreen(
                                            viewModel = viewModel,
                                            display = displayContent,
                                            onPlayAll = {
                                                playerStartIndex = 0
                                                currentScreen = "player"
                                            },
                                            onPlayItem = { index ->
                                                playerStartIndex = index
                                                currentScreen = "player"
                                            },
                                            onProfileClick = {
                                                showProfileSheet = true
                                            },
                                            modifier = Modifier.padding(innerPadding)
                                        )
                                        1 -> DisplayInfoScreen(
                                            clientId = viewModel.clientId,
                                            display = displayContent,
                                            isOnline = true,
                                            modifier = Modifier.padding(innerPadding)
                                        )
                                        2 -> ScheduleScreen(
                                            viewModel = viewModel,
                                            display = displayContent,
                                            modifier = Modifier.padding(innerPadding)
                                        )
                                        3 -> DisplayInfoScreen(
                                            clientId = viewModel.clientId,
                                            display = displayContent,
                                            isOnline = true,
                                            modifier = Modifier.padding(innerPadding)
                                        )
                                    }
                                }
                                "player" -> {
                                    displayContent?.mediaItems?.let { items ->
                                        FullscreenPlayerScreen(
                                            mediaItems = items,
                                            startIndex = playerStartIndex,
                                            onBack = { currentScreen = "main" }
                                        )
                                    } ?: run {
                                        currentScreen = "main"
                                    }
                                }
                            }
                        }

                        // Profile Settings Modal Bottom Sheet
                        if (showProfileSheet) {
                            ModalBottomSheet(
                                onDismissRequest = { showProfileSheet = false },
                                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                                containerColor = Color(0xFFF7F6F2),
                                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                                dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray) }
                            ) {
                                SettingsBottomSheetContent(
                                    firebaseAuth = firebaseAuth,
                                    clientId = viewModel.clientId,
                                    keepAwake = keepAwake,
                                    onKeepAwakeChange = { keepAwake = it },
                                    offlineSync = offlineSync,
                                    onOfflineSyncChange = { offlineSync = it },
                                    onLogout = {
                                        firebaseAuth.signOut()
                                        currentScreen = "auth"
                                        showProfileSheet = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsBottomSheetContent(
    firebaseAuth: FirebaseAuth,
    clientId: String,
    keepAwake: Boolean,
    onKeepAwakeChange: (Boolean) -> Unit,
    offlineSync: Boolean,
    onOfflineSyncChange: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val user = firebaseAuth.currentUser
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Profile & Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A2E)
        )
        Spacer(modifier = Modifier.height(20.dp))

        // User profile Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(24.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // User Photo or Initial Avatar
                if (user?.photoUrl != null) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                    )
                } else {
                    val initial = user?.displayName?.take(1)?.uppercase() ?: user?.email?.take(1)?.uppercase() ?: "P"
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .background(CardPurple, CircleShape)
                    ) {
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1A2E)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = user?.displayName ?: "PanelSena User",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E)
                    )
                    Text(
                        text = user?.email ?: "Not Authenticated",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Client ID Info Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Display Client ID",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = clientId.ifEmpty { "LOADING-ID" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E),
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(clientId))
                            Toast.makeText(context, "Client ID copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Copy ID",
                            tint = Color(0xFF1A1A2E),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Here is my PanelSena Client ID: $clientId")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Client ID"))
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Share ID",
                            tint = Color(0xFF1A1A2E),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Settings Section
        Text(
            text = "Device Preferences",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A2E)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Wake Lock Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.LightMode,
                            contentDescription = "Wake Lock",
                            tint = CardYellow
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Keep Screen On Permanently",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A2E)
                            )
                            Text(
                                text = "Prevents display from sleeping during playback",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                    Switch(
                        checked = keepAwake,
                        onCheckedChange = onKeepAwakeChange,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF1A1A2E))
                    )
                }

                Divider(color = Color(0xFFF7F6F2), thickness = 1.dp, modifier = Modifier.padding(horizontal = 12.dp))

                // Offline Sync Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CloudDownload,
                            contentDescription = "Offline Cache",
                            tint = CardSky
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Premium Offline Playback Cache",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1A2E)
                            )
                            Text(
                                text = "Pre-load media items locally when online",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                    Switch(
                        checked = offlineSync,
                        onCheckedChange = onOfflineSyncChange,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF1A1A2E))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Help & Support Accordion Section
        Text(
            text = "Help & Support",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A2E)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                FAQAccordionItem(
                    question = "How do I upload or update display content?",
                    answer = "Log into the PanelSena Web Portal, click on 'Manage Displays', register your unique Client ID, and upload files. Your remote displays will sync automatically in real-time."
                )
                Divider(color = Color(0xFFF7F6F2), thickness = 1.dp, modifier = Modifier.padding(horizontal = 12.dp))
                FAQAccordionItem(
                    question = "What media formats are supported?",
                    answer = "We support high-definition images (JPEG, PNG), seamless looping video files (MP4), and standard PDF documents."
                )
                Divider(color = Color(0xFFF7F6F2), thickness = 1.dp, modifier = Modifier.padding(horizontal = 12.dp))
                FAQAccordionItem(
                    question = "How do I contact custom developer support?",
                    answer = "For custom integrations, bulk licensing, or feature requests, contact our developer team directly at support@panelsena.app."
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Log out button
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = CardCoral),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(elevation = 4.dp, shape = RoundedCornerShape(20.dp), spotColor = CardCoral.copy(alpha = 0.3f))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color(0xFF1A1A2E)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Logout",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
            }
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun FAQAccordionItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = "Toggle",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
        
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun OfflineOverlay() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F6F2))
            .padding(24.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CardCoral),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Premium3DIllustration(
                    type = "wifi_off",
                    modifier = Modifier.size(140.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "No Connection",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFF1A1A2E),
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Content will resume automatically when you're back online.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF1A1A2E).copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }
    }
}
