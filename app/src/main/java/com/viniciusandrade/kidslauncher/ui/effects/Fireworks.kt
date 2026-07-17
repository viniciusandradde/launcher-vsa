package com.viniciusandrade.kidslauncher.ui.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

/**
 * Holds the active confetti "parties". Call [burst] with a pixel position (in the
 * KonfettiView's coordinate space) to fire a quick celebratory explosion there.
 */
class FireworksState {
    var parties by mutableStateOf<List<Party>>(emptyList())
        private set

    fun burst(x: Float, y: Float) {
        parties = listOf(
            Party(
                speed = 0f,
                maxSpeed = 34f,
                damping = 0.9f,
                spread = 360,
                colors = CONFETTI_COLORS,
                emitter = Emitter(duration = 120L, TimeUnit.MILLISECONDS).max(60),
                position = Position.Absolute(x, y),
            )
        )
    }

    private companion object {
        val CONFETTI_COLORS = listOf(
            0xFFFCE18A.toInt(),
            0xFFFF726D.toInt(),
            0xFFF4306D.toInt(),
            0xFFB48DEF.toInt(),
            0xFF66D3FA.toInt(),
            0xFF7CF29B.toInt(),
        )
    }
}

@Composable
fun rememberFireworks(): FireworksState = remember { FireworksState() }
