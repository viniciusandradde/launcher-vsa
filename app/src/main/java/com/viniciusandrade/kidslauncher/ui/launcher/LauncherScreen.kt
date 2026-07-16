package com.viniciusandrade.kidslauncher.ui.launcher

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.viniciusandrade.kidslauncher.data.model.KidProfile
import com.viniciusandrade.kidslauncher.data.model.LauncherApp
import com.viniciusandrade.kidslauncher.ui.LauncherUiState
import com.viniciusandrade.kidslauncher.ui.components.AppGridItem
import com.viniciusandrade.kidslauncher.util.TimeGreeting
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun LauncherScreen(
    state: LauncherUiState,
    onLaunch: (LauncherApp) -> Unit,
    onOpenSettings: () -> Unit,
) {
    // A launcher IS the home screen: swallow Back so a child can never "exit"
    // into a bare system UI. There is nowhere to go back to.
    BackHandler(enabled = true) { /* intentionally consumed */ }

    val profile = state.settings.activeProfile
    val iconSize = if (profile == KidProfile.PEQUENO) 88 else 64

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            Column(modifier = Modifier.fillMaxSize()) {
                GreetingHeader(profile)

                when {
                    state.loading -> LoadingState()
                    state.visibleApps.isEmpty() -> EmptyState()
                    else -> LazyVerticalGrid(
                        columns = GridCells.Fixed(profile.gridColumns),
                        contentPadding = PaddingValues(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.visibleApps, key = { it.key }) { app ->
                            AppGridItem(
                                app = app,
                                iconSize = iconSize,
                                showLabel = profile.showLabels,
                                onClick = { onLaunch(app) },
                            )
                        }
                    }
                }
            }

            // Discreet parental gate: a faint lock in the corner. Tapping it
            // always routes through the PIN screen before settings open.
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .alpha(0.35f),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = "Configurações dos pais",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun GreetingHeader(profile: KidProfile) {
    // Recompute the greeting whenever the clock crosses a minute boundary.
    val minuteTick by produceState(initialValue = 0) {
        while (true) {
            value += 1
            delay(30_000)
        }
    }
    // Recomputed whenever the minute tick advances.
    val hour = remember(minuteTick) { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = TimeGreeting.forHour(hour)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 4.dp),
    ) {
        Text(
            text = "$greeting, ${profile.displayName}! 👋",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "Escolha um app para começar",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun LoadingState() = Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

@Composable
private fun EmptyState() = Box(
    modifier = Modifier.fillMaxSize().padding(32.dp),
    contentAlignment = Alignment.Center,
) {
    Text(
        text = "Nenhum app liberado ainda.\nToque no cadeado 🔒 no canto para abrir as configurações e liberar apps.",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
    )
}
