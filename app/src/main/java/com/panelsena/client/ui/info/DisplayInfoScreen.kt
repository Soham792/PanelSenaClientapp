package com.panelsena.client.ui.info

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panelsena.client.core.theme.*
import com.panelsena.client.data.model.AssignedDisplay
import com.panelsena.client.data.model.LinkState
import com.panelsena.client.ui.components.AnimatedStatusDot
import com.panelsena.client.ui.components.InfoCard
import com.panelsena.client.ui.components.Premium3DIllustration
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DisplayInfoScreen(
    linkState: LinkState,
    display: AssignedDisplay?,
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val deviceId = when (linkState) {
        is LinkState.Unlinked -> linkState.deviceId
        is LinkState.Linked -> linkState.deviceId
        else -> ""
    }
    val isLinked = linkState is LinkState.Linked

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F6F2))
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Display Info",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFF1A1A2E),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Large Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardPurple),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = CardPurple.copy(alpha = 0.35f)
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // TV screen vector illustration
                Premium3DIllustration(
                    type = "now_playing",
                    modifier = Modifier.size(100.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = deviceId.ifEmpty { "GENERATING…" },
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    ),
                    color = Color(0xFF1A1A2E),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isLinked) "Linked Device ID" else "Your Device ID — link it in the dashboard",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF1A1A2E).copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons: Copy & Share
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(deviceId))
                            Toast.makeText(context, "Device ID copied!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = "Copy",
                                tint = Color(0xFF1A1A2E),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Copy",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF1A1A2E),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Here is my PanelSena Device ID: $deviceId")
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Device ID")
                            context.startActivity(shareIntent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A2E)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = "Share",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Share",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Info Cards List
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Link Status Card
            InfoCard(
                icon = Icons.Rounded.Link,
                iconColor = if (isLinked) StatusActive else CardYellow,
                label = "Link Status",
                value = if (isLinked) "Linked to account" else "Not linked yet"
            )

            // Display Name Card
            InfoCard(
                icon = Icons.Rounded.Tv,
                iconColor = CardPurple,
                label = "Display Name",
                value = (linkState as? LinkState.Linked)?.displayName
                    ?: display?.name?.ifEmpty { "Not Assigned Yet" } ?: "Waiting for link"
            )

            // Connection Status Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color(0xFF1A1A2E).copy(alpha = 0.08f),
                        ambientColor = Color(0xFF1A1A2E).copy(alpha = 0.04f)
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .background(CardMint.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Status",
                            tint = if (isOnline) StatusActive else StatusOffline,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Status",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Text(
                            text = if (isOnline) "Active" else "Offline",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF1A1A2E),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Pulsing animated status dot
                    AnimatedStatusDot(color = if (isOnline) StatusActive else StatusOffline)
                }
            }

            // Content Items Count Card
            val itemsCount = display?.mediaItems?.size ?: 0
            InfoCard(
                icon = Icons.Rounded.Movie,
                iconColor = CardPink,
                label = "Content Items",
                value = "$itemsCount files in queue"
            )

            // Last Updated Card
            val lastUpdatedStr = remember(display?.updatedAtMillis) {
                display?.updatedAtMillis?.let {
                    val sdf = SimpleDateFormat("MMM d, yyyy 'at' HH:mm", Locale.getDefault())
                    sdf.format(Date(it))
                } ?: "Unknown"
            }
            InfoCard(
                icon = Icons.Rounded.AccessTime,
                iconColor = CardSky,
                label = "Last Updated",
                value = lastUpdatedStr
            )
        }

        Spacer(modifier = Modifier.height(120.dp)) // Avoid navbar overlap
    }
}
