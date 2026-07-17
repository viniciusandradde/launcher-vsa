package com.viniciusandrade.kidslauncher.ui.launcher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viniciusandrade.kidslauncher.data.model.KidVideo
import com.viniciusandrade.kidslauncher.data.model.LauncherApp
import com.viniciusandrade.kidslauncher.ui.components.KidVideoCard
import com.viniciusandrade.kidslauncher.ui.components.PlayfulAppCard
import com.viniciusandrade.kidslauncher.ui.theme.KidCardColors

/**
 * Colourful "big kid board" for the youngest profile: a GCompris-style outdoor
 * scene (sky, sun, clouds, hills), a cheerful header with a time-left pill, and a
 * 2-column grid of bright app cards that pop when tapped.
 */
@Composable
fun PlayfulLauncher(
    apps: List<LauncherApp>,
    videos: List<KidVideo>,
    greeting: String,
    displayName: String,
    remainingSeconds: Int?,
    loading: Boolean,
    onLaunch: (LauncherApp) -> Unit,
    onOpenVideo: (KidVideo) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Painted scene behind everything.
        GComprisBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            PlayfulHeader(greeting, displayName, remainingSeconds)

            when {
                loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator(color = Color.White) }

                apps.isEmpty() && videos.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Nenhum app ainda! 🧸\nPeça para um adulto liberar no cadeado 🔒.",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                }

                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(videos, key = { "vid_${it.id}" }) { video ->
                        KidVideoCard(video = video, onClick = { onOpenVideo(video) })
                    }
                    items(apps, key = { "app_${it.key}" }) { app ->
                        PlayfulAppCard(
                            app = app,
                            cardColor = colorFor(app),
                            onClick = { onLaunch(app) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayfulHeader(greeting: String, displayName: String, remainingSeconds: Int?) {
    // White text with a soft shadow so it stays readable over the sky.
    val shadow = Shadow(color = Color(0x66000000), offset = Offset(0f, 2f), blurRadius = 6f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 4.dp),
    ) {
        Text(
            text = "$greeting, $displayName! 🎈",
            style = TextStyle(
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                shadow = shadow,
            ),
        )
        Text(
            text = "Toque para brincar 🚀",
            style = TextStyle(
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                shadow = shadow,
            ),
        )
        if (remainingSeconds != null) {
            val minutes = (remainingSeconds + 59) / 60 // round up
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(50),
                shadowElevation = 3.dp,
                modifier = Modifier.padding(top = 10.dp),
            ) {
                Text(
                    text = "⏳ $minutes min de brincadeira",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                )
            }
        }
    }
}

/** Stable bright colour per app so tiles keep the same colour between sessions. */
private fun colorFor(app: LauncherApp) =
    KidCardColors[Math.floorMod(app.packageName.hashCode(), KidCardColors.size)]
