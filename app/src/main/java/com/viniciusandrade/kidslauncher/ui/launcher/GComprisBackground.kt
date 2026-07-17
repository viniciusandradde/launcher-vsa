package com.viniciusandrade.kidslauncher.ui.launcher

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
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
 * A GCompris-style outdoor scene painted entirely with Compose Canvas (no image
 * assets, so it stays self-contained and crisp at any size): blue sky with a
 * smiling sun, fluffy clouds, rolling green hills and a grass band. Meant to sit
 * behind the app cards for the youngest profile.
 */
@Composable
fun GComprisBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Sky
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(SkyTop, SkyHorizon),
                startY = 0f,
                endY = h * 0.75f,
            ),
        )

        // Sun with rays (top-left, away from the parental lock in the top-right)
        drawSun(center = Offset(w * 0.17f, h * 0.09f), radius = w * 0.085f)

        // A few fluffy clouds
        drawCloud(center = Offset(w * 0.70f, h * 0.10f), unit = w * 0.045f)
        drawCloud(center = Offset(w * 0.42f, h * 0.20f), unit = w * 0.035f)
        drawCloud(center = Offset(w * 0.86f, h * 0.24f), unit = w * 0.030f)

        // Rolling hills
        drawHill(color = HillBack, topY = h * 0.66f, crest = h * 0.60f, dip = h * 0.70f)
        drawHill(color = HillFront, topY = h * 0.78f, crest = h * 0.72f, dip = h * 0.82f)

        // Grass band at the very bottom
        drawRect(color = GrassColor, topLeft = Offset(0f, h * 0.93f), size = Size(w, h * 0.07f))
    }
}

private fun DrawScope.drawSun(center: Offset, radius: Float) {
    val rayInner = radius * 1.25f
    val rayOuter = radius * 1.75f
    repeat(12) { i ->
        val a = (Math.PI / 6.0) * i
        val sx = center.x + rayInner * cos(a).toFloat()
        val sy = center.y + rayInner * sin(a).toFloat()
        val ex = center.x + rayOuter * cos(a).toFloat()
        val ey = center.y + rayOuter * sin(a).toFloat()
        drawLine(SunColor, Offset(sx, sy), Offset(ex, ey), strokeWidth = radius * 0.14f)
    }
    drawCircle(SunColor, radius, center)
}

/** Cloud = a cluster of overlapping white circles. [unit] scales the whole puff. */
private fun DrawScope.drawCloud(center: Offset, unit: Float) {
    drawCircle(CloudColor, unit * 1.0f, Offset(center.x - unit * 1.6f, center.y))
    drawCircle(CloudColor, unit * 1.4f, Offset(center.x - unit * 0.4f, center.y - unit * 0.3f))
    drawCircle(CloudColor, unit * 1.2f, Offset(center.x + unit * 1.0f, center.y))
    drawCircle(CloudColor, unit * 0.9f, Offset(center.x + unit * 1.9f, center.y + unit * 0.1f))
}

/** A single rolling hill filling from its crest line down to the bottom edge. */
private fun DrawScope.drawHill(color: androidx.compose.ui.graphics.Color, topY: Float, crest: Float, dip: Float) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(0f, topY)
        quadraticBezierTo(w * 0.25f, crest, w * 0.50f, topY)
        quadraticBezierTo(w * 0.75f, dip, w, topY - (dip - crest) * 0.3f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(path, color)
}
