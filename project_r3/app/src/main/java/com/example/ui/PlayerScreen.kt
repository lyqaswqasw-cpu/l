package com.example.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.ui.theme.DarkBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.awaitCancellation
import android.os.Handler
import android.os.Looper

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    streamUrl: String,
    streamName: String,
    playerMode: String, // Smart, m3u8, ts, Standard
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Controls visibility timer state
    var controlsVisible by remember { mutableStateOf(true) }
    var resetTimerTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(controlsVisible, resetTimerTrigger) {
        if (controlsVisible) {
            delay(3000)
            controlsVisible = false
        }
    }

    // Aspect ratio state inside the controller:
    // 0 = Fit to Screen, 1 = Full Screen, 2 = 16:9
    var aspectRatioMode by remember { mutableStateOf(0) }

    // Auto reconnect counter
    var retryCount by remember { mutableStateOf(0) }

    // Track position states for Movies & Series
    var currentPos by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }

    // Detect if content is Live stream or seekable VOD
    val isLive = remember(streamUrl) {
        streamUrl.contains("/live/") || streamUrl.contains("live")
    }

    // MANDATORY FORCE LANDSCAPE ORIENTATION ON ENTER, RESTORE ON LEAVE
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = originalOrientation
        }
    }

    // Initialize ExoPlayer clean configuration
    LaunchedEffect(streamUrl, playerMode) {
        val player = ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            
            val mediaItem = MediaItem.fromUri(streamUrl)
            val mediaSource = when (playerMode) {
                "m3u8" -> {
                    val dataSourceFactory = DefaultHttpDataSource.Factory()
                        .setAllowCrossProtocolRedirects(true)
                    HlsMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
                }
                "ts" -> {
                    val dataSourceFactory = DefaultHttpDataSource.Factory()
                        .setAllowCrossProtocolRedirects(true)
                    ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
                }
                "Smart" -> {
                    DefaultMediaSourceFactory(context).createMediaSource(mediaItem)
                }
                else -> {
                    DefaultMediaSourceFactory(context).createMediaSource(mediaItem)
                }
            }

            setMediaSource(mediaSource)
            prepare()
        }

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_READY) {
                    errorMessage = null
                    retryCount = 0
                    duration = player.duration
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                errorMessage = "خطأ في تشغيل البث: ${error.message}"
                if (playerMode == "Smart" && retryCount < 3) {
                    retryCount++
                    errorMessage = "جاري إعادة الاتصال تلقائياً (${retryCount}/3)..."
                    scopeLaunchReconnect(player, streamUrl)
                }
            }
        })

        exoPlayer = player

        try {
            awaitCancellation()
        } finally {
            player.release()
            exoPlayer = null
        }
    }

    // Dynamic timer ticker to update position forseekable slide
    LaunchedEffect(exoPlayer, isPlaying) {
        if (!isLive) {
            while (true) {
                exoPlayer?.let {
                    currentPos = it.currentPosition
                    if (it.duration > 0) {
                        duration = it.duration
                    }
                }
                delay(1000)
            }
        }
    }

    // Pulse animation logic for custom live badge (Blinking)
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable {
                // Clicking anywhere on the player screen toggles controls visibility
                controlsVisible = !controlsVisible
                if (controlsVisible) {
                    resetTimerTrigger++
                }
            }
    ) {
        // Video display view
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
                playerView.resizeMode = when (aspectRatioMode) {
                    0 -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    1 -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    2 -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                    else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = when (aspectRatioMode) {
                2 -> Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .align(Alignment.Center)
                else -> Modifier.fillMaxSize()
            }
        )

        // Overlay & Clean TOD Video Player Controller Design (with dynamic AnimatedVisibility)
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable {
                        // Clicking on the dim overlay background hides controls immediately
                        controlsVisible = false
                    }
            ) {
                // 1. Top bar controls row: Name & Exit
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { resetTimerTrigger++ } // consumes click & resets timer
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onClose() },
                        modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Close Player",
                            tint = Color.White
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isLive) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Red.copy(alpha = pulseAlpha))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "مباشر 🔴",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = streamName,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 2. Center Quick Buffering Spinner
                if (isBuffering && errorMessage == null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clickable { resetTimerTrigger++ }
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }

                // 3. Error Recover container
                if (errorMessage != null) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                            .clickable { resetTimerTrigger++ }
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                errorMessage = null
                                exoPlayer?.prepare()
                                exoPlayer?.play()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("إعادة تشغيل البث", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 4. Bottom Controls Controller Block (Video Seekbar timeline & player row)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { resetTimerTrigger++ } // consumes click & resets timer
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Seekbar seek tracks (ONLY FOR VOD/MOVIES/SERIES)
                    if (!isLive && duration > 0L) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = formatTime(currentPos),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Slider(
                                value = currentPos.toFloat().coerceIn(0f, duration.toFloat()),
                                onValueChange = { newValue ->
                                    currentPos = newValue.toLong()
                                    exoPlayer?.seekTo(newValue.toLong())
                                    resetTimerTrigger++
                                },
                                valueRange = 0f..duration.toFloat(),
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = formatTime(duration),
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Interaction controls items row (Play, back10, forward10, ratio)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Aspect ratio setting menu
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    aspectRatioMode = (aspectRatioMode + 1) % 3
                                    resetTimerTrigger++
                                },
                                modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = when (aspectRatioMode) {
                                        0 -> Icons.Default.AspectRatio
                                        1 -> Icons.Default.Fullscreen
                                        else -> Icons.Default.Tv
                                    },
                                    contentDescription = "Aspect Ratio",
                                    tint = Color.White
                                )
                            }
                            Text(
                                text = when (aspectRatioMode) {
                                    0 -> "تعبئة الشاشة"
                                    1 -> "ملء الشاشة"
                                    else -> "16:9"
                                },
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Playback actions (Rewind 10s, Play/Pause, Forward 10s)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (!isLive) {
                                IconButton(onClick = {
                                    exoPlayer?.let {
                                        val newPos = (it.currentPosition - 10000).coerceAtLeast(0)
                                        it.seekTo(newPos)
                                        currentPos = newPos
                                    }
                                    resetTimerTrigger++
                                }) {
                                    Icon(Icons.Default.Replay10, contentDescription = "Rewind 10s", tint = Color.White, modifier = Modifier.size(28.dp))
                                }
                            }

                            IconButton(
                                onClick = {
                                    exoPlayer?.let {
                                        if (it.isPlaying) {
                                            it.pause()
                                            isPlaying = false
                                        } else {
                                            it.play()
                                            isPlaying = true
                                        }
                                    }
                                    resetTimerTrigger++
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = Color.Black,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            if (!isLive) {
                                IconButton(onClick = {
                                    exoPlayer?.let {
                                        val newPos = (it.currentPosition + 10000).coerceAtMost(duration)
                                        it.seekTo(newPos)
                                        currentPos = newPos
                                    }
                                    resetTimerTrigger++
                                }) {
                                    Icon(Icons.Default.Forward10, contentDescription = "Forward 10s", tint = Color.White, modifier = Modifier.size(28.dp))
                                }
                            }
                        }

                        // Mode format status details
                        Text(
                            text = when (playerMode) {
                                "m3u8" -> "HLS (M3U8)"
                                "ts" -> "IPTV (TS)"
                                else -> "Smart Auto"
                            },
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0L) return "00:00"
    val totalSecs = ms / 1000
    val hours = totalSecs / 3600
    val mins = (totalSecs % 3600) / 60
    val secs = totalSecs % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, mins, secs)
    } else {
        String.format("%02d:%02d", mins, secs)
    }
}

@OptIn(UnstableApi::class)
private fun scopeLaunchReconnect(player: ExoPlayer, url: String) {
    Handler(Looper.getMainLooper()).post {
        try {
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
