package com.viniciusandrade.kidslauncher.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
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

/** Simple three-state navigation. A launcher has no deep back stack to manage. */
private enum class Screen { LAUNCHER, PIN, SETTINGS }

@Composable
fun KidsLauncherRoot(viewModel: KidsLauncherViewModel) {
    val state by viewModel.uiState.collectAsState()
    // Plain remember (not rememberSaveable): after a process/config change we
    // intentionally fall back to the launcher rather than leaving a child on the
    // settings screen.
    var screen by remember { mutableStateOf(Screen.LAUNCHER) }
    val context = LocalContext.current

    KidsLauncherTheme(profile = state.settings.activeProfile) {
        when (screen) {
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
            )
        }
    }
}
