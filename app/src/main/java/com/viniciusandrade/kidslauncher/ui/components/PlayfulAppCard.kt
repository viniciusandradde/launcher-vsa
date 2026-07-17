package com.viniciusandrade.kidslauncher.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import kotlin.math.hypot
import kotlin.math.sin
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.viniciusandrade.kidslauncher.data.model.LauncherApp

/**
 * A big, colourful GCompris / YouTube-Kids-style tile: a bright rounded card with
 * the app icon in a white bubble and a large bold label. It springs down while
 * pressed and pops back on release for tactile, kid-pleasing feedback.
 */
@Composable
fun PlayfulAppCard(
    app: LauncherApp,
    cardColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fingerProvider: () -> Offset? = { null },
    waveIndex: Int = 0,
    waveProvider: () -> Float? = { null },
) {
    val imageBitmap = remember(app.packageName) {
        app.icon.toBitmap().asImageBitmap()
    }

    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    // Springy scale: dips to 0.9 while held, bounces back to 1 on release.
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "cardScale",
    )
    var center by remember { mutableStateOf(Offset.Zero) }

    Surface(
        color = cardColor,
        shape = RoundedCornerShape(32.dp),
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .onGloballyPositioned { center = it.boundsInRoot().center }
            .graphicsLayer {
                // Combine the press pop with a "reach" reaction to a nearby finger.
                var s = scale
                var tx = 0f
                var ty = 0f
                val finger = fingerProvider()
                if (finger != null && center != Offset.Zero) {
                    val dx = center.x - finger.x
                    val dy = center.y - finger.y
                    val dist = hypot(dx, dy)
                    val radius = this.size.width * 1.4f
                    val react = (1f - dist / radius).coerceIn(0f, 1f)
                    s *= 1f + 0.16f * react
                    tx = dx / (dist + 1f) * 14f * react
                    ty = dy / (dist + 1f) * 14f * react
                }
                // Idle "wave": staggered vertical bob across the grid.
                val phase = waveProvider()
                if (phase != null) {
                    ty += sin(phase - waveIndex * 0.6f) * 10f
                }
                scaleX = s
                scaleY = s
                translationX = tx
                translationY = ty
            }
            .clip(RoundedCornerShape(32.dp))
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
                onClick = onClick,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    bitmap = imageBitmap,
                    contentDescription = app.label,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(56.dp),
                )
            }
            Text(
                text = app.label,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}
