package com.panelsena.client.ui.player

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.panelsena.client.data.model.MediaItem
import com.panelsena.client.data.model.MediaType
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FullscreenPlayerScreen(
    mediaItems: List<MediaItem>,
    startIndex: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) {
        var ctx = context
        while (ctx is android.content.ContextWrapper && ctx !is Activity) {
            ctx = (ctx as android.content.ContextWrapper).baseContext
        }
        ctx as? Activity
    }

    var currentIndex by remember { mutableIntStateOf(startIndex) }
    val currentItem = mediaItems.getOrNull(currentIndex)

    var overlayVisible by remember { mutableStateOf(true) }
    var overlayJob by remember { mutableStateOf<Job?>(null) }

    val scope = rememberCoroutineScope()

    // Immersive system UI hiding
    LaunchedEffect(Unit) {
        activity?.window?.insetsController?.apply {
            hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    // Restore UI on exit
    DisposableEffect(Unit) {
        onDispose {
            activity?.window?.insetsController?.apply {
                show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            }
        }
    }

    // Function to handle automatic advance
    val onAdvance = {
        currentIndex = (currentIndex + 1) % mediaItems.size
    }

    // Overlay auto-hide timer
    fun resetOverlayTimer() {
        overlayVisible = true
        overlayJob?.cancel()
        overlayJob = scope.launch {
            delay(3000)
            overlayVisible = false
        }
    }

    LaunchedEffect(currentIndex) {
        resetOverlayTimer()
    }

    if (currentItem == null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Text("No content selected", color = Color.White)
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { resetOverlayTimer() }
    ) {
        // Fullscreen Content Transitions
        AnimatedContent(
            targetState = currentIndex,
            transitionSpec = {
                fadeIn(animationSpec = tween(600)) with fadeOut(animationSpec = tween(600))
            },
            label = "fullscreen_player_transition"
        ) { targetIndex ->
            val targetItem = mediaItems[targetIndex]
            
            when (targetItem.type) {
                MediaType.IMAGE -> {
                    ImagePlayer(
                        item = targetItem,
                        onComplete = onAdvance
                    )
                }
                MediaType.VIDEO -> {
                    VideoPlayer(
                        item = targetItem,
                        onComplete = onAdvance
                    )
                }
                MediaType.PDF -> {
                    PdfPlayer(
                        item = targetItem,
                        onComplete = onAdvance
                    )
                }
            }
        }

        // Overlay elements (fade in/out)
        AnimatedVisibility(
            visible = overlayVisible,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                            )
                        )
                )

                // Back Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(top = 16.dp, start = 16.dp)
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Watermark Label
                Text(
                    text = "PanelSena Client",
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .padding(top = 22.dp, end = 22.dp)
                        .align(Alignment.TopEnd)
                )

                // Bottom gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                            )
                        )
                        .align(Alignment.BottomCenter)
                )

                // Counter text overlay bottom center
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Text(
                        text = "${currentIndex + 1} / ${mediaItems.size}  •  ${currentItem.type.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Thin Cyan queue progress bar at very top
        val progress = (currentIndex + 1).toFloat() / mediaItems.size.toFloat()
        LinearProgressIndicator(
            progress = progress,
            color = Color.Cyan,
            trackColor = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.TopCenter)
        )
    }
}

@Composable
fun ImagePlayer(
    item: MediaItem,
    onComplete: () -> Unit
) {
    var isScaled by remember { mutableStateOf(false) }
    
    // Ken burns subtle scale animation
    val scaleAnim by animateFloatAsState(
        targetValue = if (isScaled) 1.06f else 1.0f,
        animationSpec = tween(8000, easing = EaseInOutSine),
        label = "ken_burns_scale"
    )

    LaunchedEffect(item) {
        isScaled = true
        // Image displays for 8s
        delay(8000)
        onComplete()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AsyncImage(
            model = item.url,
            contentDescription = item.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(scaleX = scaleAnim, scaleY = scaleAnim)
        )
    }
}

@Composable
fun VideoPlayer(
    item: MediaItem,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    
    val exoPlayer = remember(item.url) {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = ExoMediaItem.fromUri(Uri.parse(item.url))
            setMediaItem(mediaItem)
            playWhenReady = true
            prepare()
            
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        onComplete()
                    }
                }
            })
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun PdfPlayer(
    item: MediaItem,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var pdfFile by remember { mutableStateOf<File?>(null) }
    var pagesCount by remember { mutableIntStateOf(0) }
    var currentPage by remember { mutableIntStateOf(0) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var downloadError by remember { mutableStateOf(false) }

    // Download PDF from URL in background
    LaunchedEffect(item.url) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(item.url)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()
                
                val tempFile = File.createTempFile("panelsena_", ".pdf", context.cacheDir)
                connection.inputStream.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                pdfFile = tempFile
            } catch (e: Exception) {
                downloadError = true
            }
        }
    }

    // Render page by page using android.graphics.pdf.PdfRenderer
    LaunchedEffect(pdfFile, currentPage) {
        val file = pdfFile ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            try {
                val parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val pdfRenderer = PdfRenderer(parcelFileDescriptor)
                pagesCount = pdfRenderer.pageCount
                
                val page = pdfRenderer.openPage(currentPage)
                
                // Set high quality resolution scaling
                val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                
                currentBitmap = bitmap
                page.close()
                pdfRenderer.close()
                parcelFileDescriptor.close()
            } catch (e: Exception) {
                // Render error fallback
            }
        }
    }

    // Auto-advance pages
    LaunchedEffect(pdfFile, pagesCount, currentPage) {
        if (pdfFile == null || pagesCount == 0) return@LaunchedEffect
        // Wait 6 seconds per page
        delay(6000)
        if (currentPage < pagesCount - 1) {
            currentPage++
        } else {
            onComplete() // Queue advance when PDF completes
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            downloadError -> {
                Text("Error rendering PDF", color = Color.White)
                LaunchedEffect(Unit) {
                    delay(3000)
                    onComplete() // Skip bad assets
                }
            }
            currentBitmap != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        bitmap = currentBitmap!!.asImageBitmap(),
                        contentDescription = "PDF Page",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Simple page numbers indicator
                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = "Page ${currentPage + 1} of $pagesCount",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            else -> {
                // Loading PDF visual
                CircularProgressIndicator(color = Color.Cyan)
            }
        }
    }
}
