package com.panelsena.client.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.panelsena.client.core.utils.ClientIdManager
import com.panelsena.client.ui.components.Premium3DIllustration
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    clientIdManager: ClientIdManager,
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(600, easing = EaseOutQuad)
        )
        clientIdManager.getOrCreateClientId()
        delay(600)
        onSplashComplete()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F6F2)) // Warm off-white
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(alphaAnim.value)
        ) {
            Premium3DIllustration(
                type = "now_playing",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PanelSena",
                style = MaterialTheme.typography.displayLarge,
                color = Color(0xFF1A1A2E),
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your display. Always on.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
    }
}
