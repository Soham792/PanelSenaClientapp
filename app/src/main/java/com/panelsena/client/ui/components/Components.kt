package com.panelsena.client.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.panelsena.client.ui.WeekDay
import com.panelsena.client.data.model.MediaType

@Composable
fun TypeBadge(type: MediaType) {
    Surface(
        color = Color.White.copy(alpha = 0.4f),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = type.label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF1A1A2E),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AnimatedStatusDot(color: Color, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .alpha(alpha * 0.4f)
                .background(color, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
    }
}

@Composable
fun InfoCard(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
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
                    .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF1A1A2E),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    onSeeAll: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF1A1A2E),
            fontWeight = FontWeight.Bold
        )
        if (onSeeAll != null) {
            TextButton(onClick = onSeeAll) {
                Text(
                    text = "See all →",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF6C63FF)
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    illustrationType: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Premium3DIllustration(
            type = illustrationType,
            modifier = Modifier.size(160.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF1A1A2E),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
fun Premium3DIllustration(
    type: String, // "now_playing", "waiting", "calendar", "wifi_off"
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "illustration")
    
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(
        modifier = modifier
            .graphicsLayer {
                translationY = floatOffset
                scaleX = scalePulse
                scaleY = scalePulse
            }
    ) {
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val centerY = height / 2

        when (type) {
            "now_playing" -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFB9A6F5).copy(alpha = 0.5f), Color.Transparent),
                        center = Offset(centerX, centerY),
                        radius = centerX * 1.2f
                    )
                )

                val standPath = Path().apply {
                    moveTo(centerX - 24.dp.toPx(), centerY + 30.dp.toPx())
                    lineTo(centerX + 24.dp.toPx(), centerY + 30.dp.toPx())
                    lineTo(centerX + 36.dp.toPx(), centerY + 42.dp.toPx())
                    lineTo(centerX - 36.dp.toPx(), centerY + 42.dp.toPx())
                    close()
                }
                drawPath(standPath, color = Color(0xFF1A1A2E).copy(alpha = 0.8f))

                drawRect(
                    color = Color(0xFF1A1A2E).copy(alpha = 0.9f),
                    topLeft = Offset(centerX - 6.dp.toPx(), centerY + 10.dp.toPx()),
                    size = Size(12.dp.toPx(), 20.dp.toPx())
                )

                val screenPath = Path().apply {
                    moveTo(centerX - 46.dp.toPx(), centerY - 32.dp.toPx())
                    lineTo(centerX + 46.dp.toPx(), centerY - 24.dp.toPx())
                    lineTo(centerX + 38.dp.toPx(), centerY + 18.dp.toPx())
                    lineTo(centerX - 54.dp.toPx(), centerY + 10.dp.toPx())
                    close()
                }
                drawPath(screenPath, color = Color(0xFF1A1A2E))

                val displayPath = Path().apply {
                    moveTo(centerX - 42.dp.toPx(), centerY - 28.dp.toPx())
                    lineTo(centerX + 42.dp.toPx(), centerY - 21.dp.toPx())
                    lineTo(centerX + 35.dp.toPx(), centerY + 14.dp.toPx())
                    lineTo(centerX - 50.dp.toPx(), centerY + 7.dp.toPx())
                    close()
                }
                drawPath(
                    displayPath,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFB9A6F5), Color(0xFFA6D4F5))
                    )
                )

                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    center = Offset(centerX - 10.dp.toPx(), centerY - 4.dp.toPx()),
                    radius = 12.dp.toPx()
                )
                
                val playPath = Path().apply {
                    moveTo(centerX - 14.dp.toPx(), centerY - 10.dp.toPx())
                    lineTo(centerX - 14.dp.toPx(), centerY + 2.dp.toPx())
                    lineTo(centerX - 4.dp.toPx(), centerY - 4.dp.toPx())
                    close()
                }
                drawPath(playPath, color = Color(0xFF1A1A2E))
            }
            "waiting" -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFF5C842).copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(centerX, centerY),
                        radius = centerX * 1.3f
                    )
                )

                drawCircle(
                    brush = Brush.linearGradient(listOf(Color(0xFFF5C842), Color(0xFFF5A68C))),
                    center = Offset(centerX, centerY),
                    radius = 32.dp.toPx(),
                    style = Stroke(width = 6.dp.toPx())
                )

                val rotation = (System.currentTimeMillis() % 3600) / 10f
                drawCircle(
                    color = Color(0xFF1A1A2E),
                    center = Offset(
                        centerX + (32.dp.toPx() * Math.cos(Math.toRadians(rotation.toDouble()))).toFloat(),
                        centerY + (32.dp.toPx() * Math.sin(Math.toRadians(rotation.toDouble()))).toFloat()
                    ),
                    radius = 8.dp.toPx()
                )

                drawCircle(
                    brush = Brush.linearGradient(listOf(Color.White, Color(0xFFF5C842))),
                    center = Offset(centerX, centerY),
                    radius = 14.dp.toPx()
                )
            }
            "calendar" -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFA6D4F5).copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(centerX, centerY),
                        radius = centerX * 1.2f
                    )
                )

                val calPath = Path().apply {
                    moveTo(centerX - 30.dp.toPx(), centerY - 25.dp.toPx())
                    lineTo(centerX + 30.dp.toPx(), centerY - 25.dp.toPx())
                    lineTo(centerX + 30.dp.toPx(), centerY + 25.dp.toPx())
                    lineTo(centerX - 30.dp.toPx(), centerY + 25.dp.toPx())
                    close()
                }
                drawPath(calPath, color = Color(0xFF1A1A2E))

                val calPagePath = Path().apply {
                    moveTo(centerX - 26.dp.toPx(), centerY - 15.dp.toPx())
                    lineTo(centerX + 26.dp.toPx(), centerY - 15.dp.toPx())
                    lineTo(centerX + 26.dp.toPx(), centerY + 21.dp.toPx())
                    lineTo(centerX - 26.dp.toPx(), centerY + 21.dp.toPx())
                    close()
                }
                drawPath(
                    calPagePath,
                    brush = Brush.linearGradient(listOf(Color.White, Color(0xFFA6D4F5)))
                )

                drawRect(Color(0xFF6C63FF), Offset(centerX - 18.dp.toPx(), centerY - 28.dp.toPx()), Size(4.dp.toPx(), 10.dp.toPx()))
                drawRect(Color(0xFF6C63FF), Offset(centerX + 14.dp.toPx(), centerY - 28.dp.toPx()), Size(4.dp.toPx(), 10.dp.toPx()))

                drawLine(Color(0xFF1A1A2E).copy(alpha = 0.15f), Offset(centerX - 16.dp.toPx(), centerY - 2.dp.toPx()), Offset(centerX + 16.dp.toPx(), centerY - 2.dp.toPx()), strokeWidth = 3.dp.toPx())
                drawLine(Color(0xFF1A1A2E).copy(alpha = 0.15f), Offset(centerX - 16.dp.toPx(), centerY + 8.dp.toPx()), Offset(centerX + 16.dp.toPx(), centerY + 8.dp.toPx()), strokeWidth = 3.dp.toPx())
            }
            "wifi_off" -> {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFF5A68C).copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(centerX, centerY),
                        radius = centerX * 1.3f
                    )
                )

                val sweep = 60f
                drawArc(
                    brush = Brush.linearGradient(listOf(Color(0xFFF5A68C), Color(0xFFEF4444))),
                    startAngle = 210f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(centerX - 35.dp.toPx(), centerY - 35.dp.toPx()),
                    size = Size(70.dp.toPx(), 70.dp.toPx()),
                    style = Stroke(width = 6.dp.toPx())
                )
                drawArc(
                    brush = Brush.linearGradient(listOf(Color(0xFFF5A68C), Color(0xFFEF4444))),
                    startAngle = 220f,
                    sweepAngle = 40f,
                    useCenter = false,
                    topLeft = Offset(centerX - 20.dp.toPx(), centerY - 20.dp.toPx()),
                    size = Size(40.dp.toPx(), 40.dp.toPx()),
                    style = Stroke(width = 6.dp.toPx())
                )

                drawCircle(
                    color = Color(0xFFEF4444),
                    center = Offset(centerX, centerY + 15.dp.toPx()),
                    radius = 8.dp.toPx()
                )

                drawLine(
                    color = Color(0xFF1A1A2E),
                    start = Offset(centerX - 25.dp.toPx(), centerY - 25.dp.toPx()),
                    end = Offset(centerX + 25.dp.toPx(), centerY + 25.dp.toPx()),
                    strokeWidth = 6.dp.toPx()
                )
            }
            else -> {
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFB9A6F5), Color(0xFFA6F5D4))
                    ),
                    center = Offset(centerX, centerY),
                    radius = 28.dp.toPx()
                )
            }
        }
    }
}

@Composable
fun CustomFloatingNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = remember {
        listOf(
            TabItem("Home", Icons.Rounded.Home),
            TabItem("Display", Icons.Rounded.Tv),
            TabItem("Schedule", Icons.Rounded.CalendarMonth),
            TabItem("Info", Icons.Rounded.Info)
        )
    }

    Surface(
        color = Color.White,
        shape = RoundedCornerShape(28.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = Color(0xFF1A1A2E).copy(alpha = 0.15f)
            )
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedTab == index
                
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onTabSelected(index) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (isSelected) Color(0xFF1A1A2E) else Color.Transparent,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) Color.White else Color.Gray,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        if (isSelected) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF1A1A2E),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class TabItem(val label: String, val icon: ImageVector)

@Composable
fun WeekCalendarStrip(days: List<WeekDay>, modifier: Modifier = Modifier) {
    LazyRow(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        items(days) { day ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 6.dp)
            ) {
                Text(
                    text = day.shortName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            color = if (day.isToday) Color(0xFF1A1A2E) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Text(
                        text = day.number,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (day.isToday) Color.White else Color(0xFF1A1A2E),
                        fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
