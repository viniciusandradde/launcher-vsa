package com.viniciusandrade.kidslauncher.ui.timeup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.viniciusandrade.kidslauncher.ui.theme.PlayfulBgBottom
import com.viniciusandrade.kidslauncher.ui.theme.PlayfulBgTop

/**
 * Friendly full-screen lock shown when the daily screen-time budget runs out.
 * Deliberately calm and non-punitive; only an adult can dismiss it (via PIN),
 * mirroring the YouTube Kids "time's up" screen.
 */
@Composable
fun TimeUpScreen(onUnlock: () -> Unit) {
    // The child cannot back out of this screen.
    BackHandler(enabled = true) { /* consumed */ }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(PlayfulBgTop, PlayfulBgBottom)))
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = "🌙", fontSize = 96.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Acabou o tempo por hoje!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Vamos brincar de outra coisa?\nPeça para um adulto se precisar. 💛",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        }

        // Discreet adult entry point, kept low-contrast so kids ignore it.
        TextButton(
            onClick = onUnlock,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
        ) {
            Text("Sou adulto 🔒", style = MaterialTheme.typography.labelLarge)
        }
    }
}
