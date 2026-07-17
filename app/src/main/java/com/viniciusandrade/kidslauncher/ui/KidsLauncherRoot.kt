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
import com.viniciusandrade.kidslauncher.data.model.KidVideo
import com.viniciusandrade.kidslauncher.ui.launcher.LauncherScreen
import com.viniciusandrade.kidslauncher.ui.pin.PinScreen
import com.viniciusandrade.kidslauncher.ui.player.PlayerScreen
import com.viniciusandrade.kidslauncher.ui.settings.SettingsScreen
import com.viniciusandrade.kidslauncher.ui.theme.KidsLauncherTheme
import com.viniciusandrade.kidslauncher.ui.timeup.TimeUpScreen
import kotlinx.coroutines.delay

/**
 * Screens the launcher can show. A launcher has no deep back stack.
 * PIN gates settings; PIN_TIME gates the parent "grant more time" override;
 * PLAYER hosts the in-app YouTube player.
 */
private enum class Screen { LAUNCHER, PIN, SETTINGS, TIME_UP, PIN_TIME, PLAYER }

/** How often we credit screen time while the launcher itself is on screen. */
private const val TICK_SECONDS = 15

@Composable
fun KidsLauncherRoot(viewModel: KidsLauncherViewModel) {
    val state by viewModel.uiState.collectAsState()
    // Plain remember (not rememberSaveable): after a process/config change we
    // intentionally fall back to the launcher rather than leaving a child on the
    // settings screen.
    var screen by remember { mutableStateOf(Screen.LAUNCHER) }
    var playingVideo by remember { mutableStateOf<KidVideo?>(null) }
    val context = LocalContext.current

    val profile = state.settings.activeProfile
    val hasLimit = state.settings.timeLimitMinutes(profile) > 0
    val timeUp = state.settings.isTimeUp(profile)

    // Count screen time while the child is on a "using" screen (board or player).
    val onKidScreen = screen == Screen.LAUNCHER || screen == Screen.PLAYER
    LaunchedEffect(screen, hasLimit, timeUp) {
        if (onKidScreen && hasLimit && !timeUp) {
            while (true) {
                delay(TICK_SECONDS * 1000L)
                viewModel.tickUsage(TICK_SECONDS)
            }
        }
    }

    // When the budget is spent, replace the board/player with the friendly lock —
    // unless a parent flow (PIN / settings) is already open.
    val effectiveScreen = if (timeUp && onKidScreen) Screen.TIME_UP else screen

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
                onOpenVideo = { video ->
                    // Play inside the launcher instead of leaving to YouTube.
                    playingVideo = video
                    screen = Screen.PLAYER
                },
                onOpenSettings = { screen = Screen.PIN },
            )

            Screen.PLAYER -> {
                val video = playingVideo
                if (video == null) {
                    screen = Screen.LAUNCHER
                } else {
                    PlayerScreen(
                        videoId = video.id,
                        onClose = {
                            playingVideo = null
                            screen = Screen.LAUNCHER
                        },
                    )
                }
            }

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
                onSetChildName = viewModel::setChildName,
                onAddVideo = viewModel::addVideo,
                onRemoveVideo = viewModel::removeVideo,
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
