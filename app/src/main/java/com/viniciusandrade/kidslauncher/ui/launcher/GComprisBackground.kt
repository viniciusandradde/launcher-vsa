package com.viniciusandrade.kidslauncher.ui.launcher

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.viniciusandrade.kidslauncher.ui.theme.CloudColor
import com.viniciusandrade.kidslauncher.ui.theme.GrassColor
import com.viniciusandrade.kidslauncher.ui.theme.HillBack
import com.viniciusandrade.kidslauncher.ui.theme.HillFront
import com.viniciusandrade.kidslauncher.ui.theme.SkyHorizon
import com.viniciusandrade.kidslauncher.ui.theme.SkyTop
import com.viniciusandrade.kidslauncher.ui.theme.SunColor
import kotlin.math.cos
import kotlin.math.sin

/**
 * A LIVING GCompris-style scene painted with Compose Canvas: blue sky, a sun with
 * slowly rotating rays, parallax clouds drifting at two speeds, rolling hills with
 * soft shading, a swaying grass band and daisies. Everything animates via a single
 * [rememberInfiniteTransition] and is drawn on the GPU-friendly Canvas.
 */
@Composable
fun GComprisBackground(modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "scene")

    val cloudFar by t.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(70_000, easing = LinearEasing), RepeatMode.Restart),
        label = "cloudFar",
    )
    val cloudNear by t.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(42_000, easing = LinearEasing), RepeatMode.Restart),
        label = "cloudNear",
    )
    val sunAngle by t.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(90_000, easing = LinearEasing), RepeatMode.Restart),
        label = "sunAngle",
    )
    val sway by t.animateFloat(
        initialValue = -1f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3800), RepeatMode.Reverse),
        label = "sway",
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(listOf(SkyTop, SkyHorizon), startY = 0f, endY = h * 0.75f),
        )

        drawAnimatedSun(center = Offset(w * 0.17f, h * 0.09f), radius = w * 0.085f, angle = sunAngle)

        // Parallax clouds: far layer (slow, smaller) and near layer (faster, bigger).
        drawDriftingCloud(cloudFar, baseFrac = 0.10f, y = h * 0.09f, unit = w * 0.030f)
        drawDriftingCloud(cloudFar, baseFrac = 0.65f, y = h * 0.15f, unit = w * 0.034f)
        drawDriftingCloud(cloudNear, baseFrac = 0.35f, y = h * 0.20f, unit = w * 0.048f)
        drawDriftingCloud(cloudNear, baseFrac = 0.85f, y = h * 0.26f, unit = w * 0.040f)

        // Rolling hills with a lighter crest band for a bit of shading/texture.
        drawHill(HillBack, topY = h * 0.66f, crest = h * 0.60f, dip = h * 0.70f)
        drawHill(HillFront, topY = h * 0.78f, crest = h * 0.72f, dip = h * 0.82f)

        // Grass band + swaying blades.
        drawRect(color = GrassColor, topLeft = Offset(0f, h * 0.93f), size = Size(w, h * 0.07f))
        drawGrassBlades(baseY = h * 0.94f, sway = sway)

        // A few daisies scattered on the near hill.
        drawDaisy(Offset(w * 0.30f, h * 0.86f), r = w * 0.012f)
        drawDaisy(Offset(w * 0.62f, h * 0.90f), r = w * 0.014f)
        drawDaisy(Offset(w * 0.82f, h * 0.84f), r = w * 0.011f)
    }
}

private fun DrawScope.drawAnimatedSun(center: Offset, radius: Float, angle: Float) {
    rotate(degrees = angle, pivot = center) {
        val rayInner = radius * 1.25f
        val rayOuter = radius * 1.8f
        repeat(12) { i ->
            val a = (Math.PI / 6.0) * i
            val sx = center.x + rayInner * cos(a).toFloat()
            val sy = center.y + rayInner * sin(a).toFloat()
            val ex = center.x + rayOuter * cos(a).toFloat()
            val ey = center.y + rayOuter * sin(a).toFloat()
            drawLine(SunColor, Offset(sx, sy), Offset(ex, ey), strokeWidth = radius * 0.14f)
        }
    }
    drawCircle(SunColor, radius, center)
}

/** [progress] 0..1 loops the cloud across the screen; [baseFrac] staggers them. */
private fun DrawScope.drawDriftingCloud(progress: Float, baseFrac: Float, y: Float, unit: Float) {
    val w = size.width
    val travel = w + unit * 6f
    val x = -unit * 3f + ((baseFrac + progress) % 1f) * travel
    val c = Offset(x, y)
    drawCircle(CloudColor, unit * 1.0f, Offset(c.x - unit * 1.6f, c.y))
    drawCircle(CloudColor, unit * 1.4f, Offset(c.x - unit * 0.4f, c.y - unit * 0.3f))
    drawCircle(CloudColor, unit * 1.2f, Offset(c.x + unit * 1.0f, c.y))
    drawCircle(CloudColor, unit * 0.9f, Offset(c.x + unit * 1.9f, c.y + unit * 0.1f))
}

private fun DrawScope.drawHill(color: Color, topY: Float, crest: Float, dip: Float) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(0f, topY)
        quadraticBezierTo(w * 0.25f, crest, w * 0.50f, topY)
        quadraticBezierTo(w * 0.75f, dip, w, topY - (dip - crest) * 0.3f)
        lineTo(w, h); lineTo(0f, h); close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawGrassBlades(baseY: Float, sway: Float) {
    val w = size.width
    val h = size.height
    val bladeColor = Color(0xFF558B2F)
    var x = w * 0.03f
    var i = 0
    while (x < w) {
        val height = h * (0.03f + 0.02f * ((i % 3) / 2f))
        val tilt = sway * (w * 0.012f) * (if (i % 2 == 0) 1f else -1f)
        val path = Path().apply {
            moveTo(x - w * 0.006f, baseY + height)
            quadraticBezierTo(x + tilt, baseY, x + w * 0.006f, baseY + height)
            close()
        }
        drawPath(path, bladeColor)
        x += w * 0.045f
        i++
    }
}

private fun DrawScope.drawDaisy(center: Offset, r: Float) {
    repeat(6) { i ->
        val a = (Math.PI / 3.0) * i
        val px = center.x + r * cos(a).toFloat()
        val py = center.y + r * sin(a).toFloat()
        drawCircle(Color.White, r * 0.6f, Offset(px, py))
    }
    drawCircle(Color(0xFFFFD54F), r * 0.7f, center)
}
