package com.viniciusandrade.kidslauncher.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import coil.compose.AsyncImage
import com.viniciusandrade.kidslauncher.data.model.KidVideo

/**
 * A YouTube video tile: the video thumbnail with a big red play badge and the
 * video name. Shares the springy press feedback of the app cards. Tapping opens
 * the video. The red background shows through while the thumbnail loads/fails.
 */
@Composable
fun KidVideoCard(
    video: KidVideo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fingerProvider: () -> Offset? = { null },
    waveIndex: Int = 0,
    waveProvider: () -> Float? = { null },
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "videoScale",
    )
    var center by remember { mutableStateOf(Offset.Zero) }

    Surface(
        color = Color(0xFFD32F2F), // YouTube-ish red as the loading backdrop
        shape = RoundedCornerShape(32.dp),
        shadowElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .onGloballyPositioned { center = it.boundsInRoot().center }
            .graphicsLayer {
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
        Box(contentAlignment = Alignment.Center) {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = video.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )

            // Dark scrim at the bottom so the label is always legible.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.55f to Color.Transparent,
                            1f to Color(0xCC000000),
                        )
                    )
            )

            // Play badge in the middle.
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xE6FF0000)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp),
                )
            }

            Text(
                text = video.displayName,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
            )
        }
    }
}
