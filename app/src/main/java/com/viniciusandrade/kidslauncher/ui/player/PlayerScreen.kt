package com.viniciusandrade.kidslauncher.ui.player

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.clickable
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

/**
 * In-app YouTube playback: the IFrame player embedded via [YouTubePlayerView], so
 * the video plays *inside* the launcher and closing returns to the grid — the
 * child never lands in the full YouTube app. `rel(0)` reduces related-video
 * suggestions at the end.
 */
@Composable
fun PlayerScreen(
    videoId: String,
    onClose: () -> Unit,
) {
    BackHandler(enabled = true) { onClose() }

    val lifecycleOwner = LocalLifecycleOwner.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            factory = { context ->
                YouTubePlayerView(context).apply {
                    enableAutomaticInitialization = false
                    lifecycleOwner.lifecycle.addObserver(this)
                    val options = IFramePlayerOptions.Builder()
                        .controls(1)
                        .rel(0)
                        .build()
                    initialize(
                        object : AbstractYouTubePlayerListener() {
                            override fun onReady(youTubePlayer: YouTubePlayer) {
                                youTubePlayer.loadVideo(videoId, 0f)
                            }
                        },
                        options,
                    )
                }
            },
            onRelease = { it.release() },
            modifier = Modifier.fillMaxWidth(),
        )

        // Big, friendly close button.
        Surface(
            color = Color(0xE6FF0000),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clip(CircleShape)
                .clickable(onClick = onClose),
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Fechar vídeo",
                tint = Color.White,
                modifier = Modifier.padding(12.dp),
            )
        }
    }
}
