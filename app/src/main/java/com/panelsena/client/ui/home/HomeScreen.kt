package com.panelsena.client.ui.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Close
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
import com.panelsena.client.core.theme.*
import com.panelsena.client.data.model.AssignedDisplay
import com.panelsena.client.data.model.MediaItem
import com.panelsena.client.data.model.MediaType
import com.panelsena.client.ui.DisplayViewModel
import com.panelsena.client.ui.components.*

import androidx.compose.foundation.text.BasicTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: DisplayViewModel,
    display: AssignedDisplay?,
    onPlayAll: () -> Unit,
    onPlayItem: (Int) -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F6F2))
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Expanding, Animated Search Bar or Top Bar
        if (searchActive) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.White, RoundedCornerShape(28.dp))
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(28.dp), spotColor = Color(0xFF1A1A2E).copy(alpha = 0.08f))
                    .padding(horizontal = 20.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF1A1A2E),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = Color(0xFF1A1A2E),
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    ),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search media items...",
                                    color = Color.Gray.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontSize = 16.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                IconButton(
                    onClick = {
                        searchQuery = ""
                        searchActive = false
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            HomeTopBar(
                deviceId = viewModel.deviceId,
                date = viewModel.todayDate,
                onProfileClick = onProfileClick,
                onSearchClick = { searchActive = true }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hero Card
        HeroStatusCard(
            display = display,
            deviceId = viewModel.deviceId
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Calendar strip
        WeekCalendarStrip(days = viewModel.weekDays)

        Spacer(modifier = Modifier.height(20.dp))

        // Content header
        SectionHeader(
            title = if (searchQuery.isNotEmpty()) "Search Results" else "Your Content",
            onSeeAll = {}
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Media List / Grid with Search Filtering
        if (display == null || display.assignedMediaUrls.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text(
                    text = "No content loaded yet.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        } else {
            val mediaItems = display.mediaItems
            val filteredItems = remember(mediaItems, searchQuery) {
                if (searchQuery.isBlank()) {
                    mediaItems
                } else {
                    mediaItems.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                        it.type.label.contains(searchQuery, ignoreCase = true)
                    }
                }
            }
            
            if (filteredItems.isEmpty()) {
                EmptyState(
                    illustrationType = "wifi_off",
                    title = "No results found",
                    subtitle = "Try searching for a different file name or media type.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        itemsIndexed(filteredItems) { index, item ->
                            val colors = listOf(CardYellow, CardSky, CardMint, CardPink)
                            val cardColor = colors[index % colors.size]
                            
                            // Get real index in primary list
                            val originalIndex = mediaItems.indexOf(item).coerceAtLeast(0)
                            
                            MediaItemCard(
                                item = item,
                                cardColor = cardColor,
                                onClick = { onPlayItem(originalIndex) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Play All Button
                    Button(
                        onClick = onPlayAll,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A2E)),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(24.dp),
                                spotColor = Color(0xFF1A1A2E).copy(alpha = 0.25f)
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Play All Content",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(96.dp)) // Avoid navbar overlap
                }
            }
        }
    }
}

@Composable
fun HomeTopBar(
    deviceId: String,
    date: String,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val userName = remember {
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val name = user?.displayName ?: user?.email?.substringBefore("@") ?: "User"
        name
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Colored circular avatar representing first letter of the signed-in user
        val letter = userName.take(1).uppercase().ifEmpty { "P" }
        val avatarColors = listOf(CardPurple, CardYellow, CardPink, CardSky, CardMint)
        val avatarBg = remember(deviceId) { avatarColors.random() }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .background(avatarBg, CircleShape)
                .shadow(elevation = 2.dp, shape = CircleShape)
                .clickable { onProfileClick() }
        ) {
            Text(
                text = letter,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF1A1A2E),
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hello, $userName",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1A1A2E),
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Text(
                text = "Today, $date",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }

        // Search icon button
        IconButton(
            onClick = onSearchClick,
            modifier = Modifier
                .size(44.dp)
                .background(Color.White, CircleShape)
                .shadow(elevation = 4.dp, shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search",
                tint = Color(0xFF1A1A2E)
            )
        }
    }
}

@Composable
fun HeroStatusCard(display: AssignedDisplay?, deviceId: String) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    if (display != null && display.mediaItems.isNotEmpty()) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Now Playing",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF1A1A2E).copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = display.name.ifEmpty { "Lobby Display" },
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color(0xFF1A1A2E),
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${display.mediaItems.size} items in queue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1A1A2E).copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    MediaTypeIconRow(mediaTypes = display.mediaTypes)
                }

                // Premium 3D float screen illustration
                Premium3DIllustration(
                    type = "now_playing",
                    modifier = Modifier.size(100.dp)
                )
            }
        }
    } else {
        // Waiting state hero card (CardYellow)
        Card(
            colors = CardDefaults.cardColors(containerColor = CardYellow),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = CardYellow.copy(alpha = 0.3f)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Waiting for content...",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color(0xFF1A1A2E),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Link this device using its Device ID",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF1A1A2E).copy(alpha = 0.8f)
                        )
                    }
                    Premium3DIllustration(
                        type = "waiting",
                        modifier = Modifier.size(72.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Copy Device ID Chip
                Surface(
                    color = Color.White.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            clipboardManager.setText(AnnotatedString(deviceId))
                            Toast
                                .makeText(context, "Device ID copied!", Toast.LENGTH_SHORT)
                                .show()
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = deviceId.ifEmpty { "GENERATING…" },
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF1A1A2E),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color(0xFF1A1A2E),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MediaTypeIconRow(mediaTypes: List<String>) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val distinctTypes = mediaTypes.map { it.lowercase() }.distinct()
        distinctTypes.forEach { type ->
            val label = when (type) {
                "image" -> "Image"
                "video" -> "Video"
                "pdf" -> "PDF"
                else -> type.replaceFirstChar { it.uppercase() }
            }
            Surface(
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF1A1A2E),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}



@Composable
fun MediaItemCard(
    item: MediaItem,
    cardColor: Color,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = cardColor.copy(alpha = 0.25f)
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                TypeBadge(type = item.type)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = item.name,
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1A1A2E),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 24.sp,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = item.meta,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF1A1A2E).copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Source",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF1A1A2E).copy(alpha = 0.5f)
                    )
                    Text(
                        text = "Firebase",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF1A1A2E),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                
                // Illustrated vector balloon/orb
                Premium3DIllustration(
                    type = "item_card",
                    modifier = Modifier.size(44.dp)
                )
            }
        }
    }
}
