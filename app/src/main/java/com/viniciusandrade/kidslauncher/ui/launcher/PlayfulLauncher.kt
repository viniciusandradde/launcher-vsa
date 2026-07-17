package com.viniciusandrade.kidslauncher.ui.launcher

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viniciusandrade.kidslauncher.data.model.LauncherApp
import com.viniciusandrade.kidslauncher.ui.components.PlayfulAppCard
import com.viniciusandrade.kidslauncher.ui.theme.KidCardColors
import com.viniciusandrade.kidslauncher.ui.theme.PlayfulBgBottom
import com.viniciusandrade.kidslauncher.ui.theme.PlayfulBgTop

/**
 * Colourful "big kid board" for the youngest profile: a soft gradient backdrop,
 * a cheerful header with a time-left pill, and a 2-column grid of bright app cards.
 */
@Composable
fun PlayfulLauncher(
    apps: List<LauncherApp>,
    greeting: String,
    profileName: String,
    remainingSeconds: Int?,
    loading: Boolean,
    onLaunch: (LauncherApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PlayfulBgTop, PlayfulBgBottom))),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PlayfulHeader(greeting, profileName, remainingSeconds)

            when {
                loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

                apps.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Nenhum app ainda! 🧸\nPeça para um adulto liberar no cadeado 🔒.",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
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
                    items(apps, key = { it.key }) { app ->
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
private fun PlayfulHeader(greeting: String, profileName: String, remainingSeconds: Int?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 4.dp),
    ) {
        Text(
            text = "$greeting, $profileName! 🎈",
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Toque para brincar 🚀",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        if (remainingSeconds != null) {
            val minutes = (remainingSeconds + 59) / 60 // round up
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(50),
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
