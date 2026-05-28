package com.panelsena.client.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panelsena.client.core.theme.*
import com.panelsena.client.data.model.AssignedDisplay
import com.panelsena.client.data.model.MediaItem
import com.panelsena.client.ui.DisplayViewModel
import com.panelsena.client.ui.components.*

@Composable
fun ScheduleScreen(
    viewModel: DisplayViewModel,
    display: AssignedDisplay?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F6F2))
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Today's Schedule",
            style = MaterialTheme.typography.headlineLarge,
            color = Color(0xFF1A1A2E),
            fontWeight = FontWeight.Bold
        )
        Text(
            text = viewModel.todayDate,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Week Calendar Strip
        WeekCalendarStrip(days = viewModel.weekDays)

        Spacer(modifier = Modifier.height(24.dp))

        // Scheduled Items Timeline
        if (display == null || display.assignedMediaUrls.isEmpty()) {
            EmptyState(
                illustrationType = "calendar",
                title = "No Content Scheduled",
                subtitle = "Assigned files will appear here in chronological order.",
                modifier = Modifier.weight(1f)
            )
        } else {
            val mediaItems = display.mediaItems
            val scheduledItems = remember(mediaItems) {
                // Synthesize scheduled times to make the calendar look alive and premium!
                var currentHour = 9
                mediaItems.mapIndexed { index, item ->
                    val startHour = String.format("%02d:00", currentHour)
                    currentHour = (currentHour + 1) % 24
                    val endHour = String.format("%02d:00", currentHour)
                    val timeRange = "$startHour - $endHour"
                    
                    ScheduledItem(
                        item = item,
                        timeRange = timeRange,
                        duration = when (item.type) {
                            com.panelsena.client.data.model.MediaType.IMAGE -> "Duration: 8 seconds"
                            com.panelsena.client.data.model.MediaType.VIDEO -> "Looping Video"
                            com.panelsena.client.data.model.MediaType.PDF -> "Dynamic PDF"
                        }
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(scheduledItems) { index, scheduledItem ->
                    val colors = listOf(CardYellow, CardSky, CardMint, CardPink)
                    val cardColor = colors[index % colors.size]

                    ScheduleItemCard(
                        scheduledItem = scheduledItem,
                        cardColor = cardColor
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(120.dp)) // Avoid bottom nav overlap
                }
            }
        }
    }
}

@Composable
fun ScheduleItemCard(
    scheduledItem: ScheduledItem,
    cardColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = cardColor.copy(alpha = 0.25f)
            )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                TypeBadge(type = scheduledItem.item.type)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = scheduledItem.item.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF1A1A2E),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = scheduledItem.timeRange,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF1A1A2E).copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = scheduledItem.duration,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF1A1A2E).copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Illustrated vector balloon/orb
            Premium3DIllustration(
                type = "calendar_item",
                modifier = Modifier.size(60.dp)
            )
        }
    }
}

data class ScheduledItem(
    val item: MediaItem,
    val timeRange: String,
    val duration: String
)
