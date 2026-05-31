package com.panelsena.client.ui.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.panelsena.client.core.theme.CardPurple
import com.panelsena.client.core.theme.CardSky
import com.panelsena.client.core.theme.CardYellow
import com.panelsena.client.core.theme.DmSansFontFamily
import com.panelsena.client.core.theme.SyneFontFamily
import com.panelsena.client.ui.components.Premium3DIllustration
import kotlinx.coroutines.launch

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun AuthScreen(
    firebaseAuth: FirebaseAuth,
    onSignInSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    // Configure Google Sign In.
    // The web client ID is sourced from google-services.json (default_web_client_id) so it
    // always matches the active Firebase project (panelsena-r2) instead of being hardcoded.
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(com.panelsena.client.R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    
    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    // Auth launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                scope.launch {
                    isLoading = true
                    firebaseAuth.signInWithCredential(credential)
                        .addOnCompleteListener { authResult ->
                            isLoading = false
                            if (authResult.isSuccessful) {
                                onSignInSuccess()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Firebase Authentication Failed: ${authResult.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            } catch (e: ApiException) {
                isLoading = false
                Toast.makeText(
                    context,
                    "Google Sign-In Failed: ${e.message} (Code: ${e.statusCode})",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            isLoading = false
        }
    }

    // Gorgeous gradient background animations
    val infiniteTransition = rememberInfiniteTransition(label = "auth_bg")
    val animOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_orb1"
    )
    val animOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -120f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_orb2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F6F2)) // Warm off-white
    ) {
        // Floating glassmorphic procedural ambient blobs
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CardPurple.copy(alpha = 0.25f), Color.Transparent),
                ),
                center = Offset(size.width * 0.2f + animOffset1.dp.toPx(), size.height * 0.3f),
                radius = size.width * 0.6f
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CardSky.copy(alpha = 0.2f), Color.Transparent),
                ),
                center = Offset(size.width * 0.8f, size.height * 0.7f + animOffset2.dp.toPx()),
                radius = size.width * 0.7f
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Main Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.75f)),
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(32.dp),
                        spotColor = Color(0xFF1A1A2E).copy(alpha = 0.15f)
                    )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Premium3DIllustration(
                        type = "now_playing",
                        modifier = Modifier.size(130.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Welcome to PanelSena",
                        style = MaterialTheme.typography.headlineLarge,
                        fontFamily = SyneFontFamily,
                        color = Color(0xFF1A1A2E),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Sign in to connect and manage your remote client displays in real-time.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = DmSansFontFamily,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color(0xFF1A1A2E),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Authenticating securely...",
                            fontFamily = DmSansFontFamily,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Gray
                        )
                    } else {
                        // Google Sign-In Button
                        Button(
                            onClick = {
                                isLoading = true
                                val signInIntent = googleSignInClient.signInIntent
                                launcher.launch(signInIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    spotColor = Color(0xFF1A1A2E).copy(alpha = 0.12f)
                                )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Draw Vector Google "G" logo on Canvas
                                Canvas(modifier = Modifier.size(22.dp)) {
                                    val r = size.width / 2
                                    val stroke = 3.2.dp.toPx()
                                    
                                    // Google Red Segment
                                    drawArc(
                                        color = Color(0xFFEA4335),
                                        startAngle = 135f,
                                        sweepAngle = 90f,
                                        useCenter = false,
                                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                                    )
                                    // Google Yellow Segment
                                    drawArc(
                                        color = Color(0xFFFBBC05),
                                        startAngle = 45f,
                                        sweepAngle = 90f,
                                        useCenter = false,
                                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                                    )
                                    // Google Green Segment
                                    drawArc(
                                        color = Color(0xFF34A853),
                                        startAngle = -45f,
                                        sweepAngle = 90f,
                                        useCenter = false,
                                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                                    )
                                    // Google Blue Segment & Horizontal bar
                                    drawArc(
                                        color = Color(0xFF4285F4),
                                        startAngle = -135f,
                                        sweepAngle = 90f,
                                        useCenter = false,
                                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                                    )
                                    drawLine(
                                        color = Color(0xFF4285F4),
                                        start = Offset(r, r),
                                        end = Offset(size.width, r),
                                        strokeWidth = stroke,
                                        cap = StrokeCap.Round
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(14.dp))
                                
                                Text(
                                    text = "Continue with Google",
                                    fontFamily = DmSansFontFamily,
                                    fontSize = 16.sp,
                                    color = Color(0xFF1A1A2E),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer Text
            Text(
                text = "Secured by Firebase and Google Services",
                fontFamily = DmSansFontFamily,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
