package com.viniciusandrade.kidslauncher.ui.launcher

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viniciusandrade.kidslauncher.data.model.KidVideo
import com.viniciusandrade.kidslauncher.data.model.LauncherApp
import com.viniciusandrade.kidslauncher.ui.components.KidVideoCard
import com.viniciusandrade.kidslauncher.ui.components.PlayfulAppCard
import com.viniciusandrade.kidslauncher.ui.effects.rememberFireworks
import com.viniciusandrade.kidslauncher.ui.theme.KidCardColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.compose.KonfettiView

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
    // Finger tracking: `finger` (nullable) drives the card "reach" reaction; the
    // last known point is where taps set off fireworks.
    // `finger` is in ROOT space (matches cards' boundsInRoot); `lastPoint` is in
    // this Box's local space (matches the KonfettiView drawn in the same Box).
    var finger by remember { mutableStateOf<Offset?>(null) }
    val lastPoint = remember { mutableStateOf(Offset.Zero) }
    var boxPos by remember { mutableStateOf(Offset.Zero) }
    val fireworks = rememberFireworks()
    val scope = rememberCoroutineScope()

    // Idle "attention" show after 10s without a touch. Any down bumps the tick,
    // which cancels and restarts the timer.
    var idle by remember { mutableStateOf(false) }
    var interactionTick by remember { mutableStateOf(0) }
    LaunchedEffect(interactionTick) {
        idle = false
        delay(10_000)
        idle = true
    }

    // Continuous wave phase used for the idle "ola" across the cards.
    val waveT = rememberInfiniteTransition(label = "wave")
    val wavePhase by waveT.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "wavePhase",
    )
    val waveProvider: () -> Float? = { if (idle) wavePhase else null }

    fun celebrateThen(action: () -> Unit): () -> Unit = {
        val p = lastPoint.value
        fireworks.burst(p.x, p.y)
        scope.launch {
            delay(220) // let the burst show before we leave the screen
            action()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { boxPos = it.positionInRoot() }
            .pointerInput(Unit) {
                // Observe (don't consume) pointers so cards still receive clicks.
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    finger = boxPos + down.position
                    lastPoint.value = down.position
                    interactionTick++ // reset the idle timer
                    do {
                        val event = awaitPointerEvent()
                        event.changes.lastOrNull()?.let {
                            finger = boxPos + it.position
                            lastPoint.value = it.position
                        }
                    } while (event.changes.any { it.pressed })
                    finger = null
                }
            },
    ) {
        // Painted, living scene behind everything.
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
                    itemsIndexed(videos, key = { _, v -> "vid_${v.id}" }) { index, video ->
                        KidVideoCard(
                            video = video,
                            onClick = celebrateThen { onOpenVideo(video) },
                            fingerProvider = { finger },
                            waveIndex = index,
                            waveProvider = waveProvider,
                        )
                    }
                    itemsIndexed(apps, key = { _, a -> "app_${a.key}" }) { index, app ->
                        PlayfulAppCard(
                            app = app,
                            cardColor = colorFor(app),
                            onClick = celebrateThen { onLaunch(app) },
                            fingerProvider = { finger },
                            waveIndex = videos.size + index,
                            waveProvider = waveProvider,
                        )
                    }
                }
            }
        }

        // Idle "attention" show (mascot + shooting star), non-interactive.
        IdleOverlay(visible = idle)

        // Fireworks overlay on top of everything.
        KonfettiView(
            modifier = Modifier.fillMaxSize(),
            parties = fireworks.parties,
        )
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
