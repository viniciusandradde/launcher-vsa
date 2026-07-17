package com.viniciusandrade.kidslauncher.ui.launcher

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The "attention" show that plays after ~10s of inactivity: a mascot strolls across
 * the grass waving, and a shooting star with sparkles streaks across the sky. Purely
 * decorative (emoji-based, no assets) and non-interactive, so taps pass through.
 */
@Composable
fun IdleOverlay(visible: Boolean, modifier: Modifier = Modifier) {
    if (!visible) return

    val t = rememberInfiniteTransition(label = "idle")
    val cross by t.animateFloat(
        0f, 1f, infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Restart),
        label = "cross",
    )
    val bob by t.animateFloat(
        0f, 1f, infiniteRepeatable(tween(480), RepeatMode.Reverse),
        label = "bob",
    )
    val wave by t.animateFloat(
        -1f, 1f, infiniteRepeatable(tween(320), RepeatMode.Reverse),
        label = "wave",
    )
    val star by t.animateFloat(
        0f, 1f, infiniteRepeatable(tween(3600, easing = LinearEasing), RepeatMode.Restart),
        label = "star",
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val w = maxWidth
        val h = maxHeight

        // Shooting star: diagonal streak that fades as it goes, with a sparkle.
        val starX = -40.dp + (w + 80.dp) * star
        val starY = h * 0.06f + h * 0.14f * star
        Text(
            text = "⭐",
            style = TextStyle(fontSize = 30.sp),
            modifier = Modifier.offset(x = starX, y = starY).alpha((1f - star).coerceIn(0f, 1f)),
        )
        Text(
            text = "✨",
            style = TextStyle(fontSize = 20.sp),
            modifier = Modifier
                .offset(x = starX - 26.dp, y = starY - 10.dp)
                .alpha(((1f - star) * 0.8f).coerceIn(0f, 1f)),
        )

        // Mascot walking across the grass, bobbing, with a waving hand.
        val mascotX = (-70).dp + (w + 140.dp) * cross
        val mascotY = h * 0.82f - (bob * 8f).dp
        Text(
            text = "🦖",
            style = TextStyle(fontSize = 52.sp),
            modifier = Modifier.offset(x = mascotX, y = mascotY),
        )
        Text(
            text = "👋",
            style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Normal, color = Color.Unspecified),
            modifier = Modifier
                .offset(x = mascotX + 40.dp, y = mascotY - 6.dp)
                .rotate(wave * 18f),
        )
    }
}
