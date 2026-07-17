package com.viniciusandrade.kidslauncher.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.viniciusandrade.kidslauncher.ui.launcher.LauncherScreen
import com.viniciusandrade.kidslauncher.ui.pin.PinScreen
import com.viniciusandrade.kidslauncher.ui.settings.SettingsScreen
import com.viniciusandrade.kidslauncher.ui.theme.KidsLauncherTheme
import com.viniciusandrade.kidslauncher.ui.timeup.TimeUpScreen
import kotlinx.coroutines.delay

/**
 * Screens the launcher can show. A launcher has no deep back stack.
 * PIN gates settings; PIN_TIME gates the parent "grant more time" override.
 */
private enum class Screen { LAUNCHER, PIN, SETTINGS, TIME_UP, PIN_TIME }

/** How often we credit screen time while the launcher itself is on screen. */
private const val TICK_SECONDS = 15

@Composable
fun KidsLauncherRoot(viewModel: KidsLauncherViewModel) {
    val state by viewModel.uiState.collectAsState()
    // Plain remember (not rememberSaveable): after a process/config change we
    // intentionally fall back to the launcher rather than leaving a child on the
    // settings screen.
    var screen by remember { mutableStateOf(Screen.LAUNCHER) }
    val context = LocalContext.current

    val profile = state.settings.activeProfile
    val hasLimit = state.settings.timeLimitMinutes(profile) > 0
    val timeUp = state.settings.isTimeUp(profile)

    // Count screen time while the child is actually on the launcher board.
    LaunchedEffect(screen, hasLimit, timeUp) {
        if (screen == Screen.LAUNCHER && hasLimit && !timeUp) {
            while (true) {
                delay(TICK_SECONDS * 1000L)
                viewModel.tickUsage(TICK_SECONDS)
            }
        }
    }

    // When the budget is spent, replace the board with the friendly lock —
    // unless a parent flow (PIN / settings) is already open.
    val effectiveScreen = if (timeUp && screen == Screen.LAUNCHER) Screen.TIME_UP else screen

    KidsLauncherTheme(profile = profile) {
        when (effectiveScreen) {
            Screen.LAUNCHER -> LauncherScreen(
                state = state,
                onLaunch = { app ->
                    viewModel.launch(app) {
                        Toast.makeText(
                            context,
                            "Não consegui abrir ${app.label}",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                },
                onOpenSettings = { screen = Screen.PIN },
            )

            Screen.PIN -> PinScreen(
                title = "Digite o PIN dos pais",
                onVerify = viewModel::verifyPin,
                onSuccess = { screen = Screen.SETTINGS },
                onCancel = { screen = Screen.LAUNCHER },
            )

            Screen.SETTINGS -> SettingsScreen(
                state = state,
                onBack = { screen = Screen.LAUNCHER },
                onSelectProfile = viewModel::switchProfile,
                onToggleApp = viewModel::setAppAllowed,
                onChangePin = viewModel::setPin,
                onSetTimeLimit = viewModel::setTimeLimit,
            )

            Screen.TIME_UP -> TimeUpScreen(
                onUnlock = { screen = Screen.PIN_TIME },
            )

            Screen.PIN_TIME -> PinScreen(
                title = "Digite o PIN dos pais",
                onVerify = viewModel::verifyPin,
                onSuccess = {
                    viewModel.grantMoreTime()
                    screen = Screen.LAUNCHER
                },
                onCancel = { screen = Screen.TIME_UP },
            )
        }
    }
}
