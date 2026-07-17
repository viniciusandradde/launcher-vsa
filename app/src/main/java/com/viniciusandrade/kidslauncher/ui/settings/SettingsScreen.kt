package com.viniciusandrade.kidslauncher.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Password
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.viniciusandrade.kidslauncher.data.AppSettings
import com.viniciusandrade.kidslauncher.data.model.KidProfile
import com.viniciusandrade.kidslauncher.ui.LauncherUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: LauncherUiState,
    onBack: () -> Unit,
    onSelectProfile: (KidProfile) -> Unit,
    onToggleApp: (KidProfile, String, Boolean) -> Unit,
    onChangePin: (String) -> Unit,
    onSetTimeLimit: (KidProfile, Int) -> Unit,
    onSetChildName: (KidProfile, String) -> Unit,
) {
    // The profile currently being configured is the active one; selecting a chip
    // both activates it and switches which whitelist we edit.
    val editing = state.settings.activeProfile
    val allowed = state.settings.allowedPackages(editing)
    var showPinDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações dos pais") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Voltar")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
        ) {
            item {
                SectionTitle("Perfil ativo")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    KidProfile.entries.forEach { profile ->
                        FilterChip(
                            selected = profile == editing,
                            onClick = { onSelectProfile(profile) },
                            label = { Text("${profile.displayName} (${profile.ageLabel})") },
                        )
                    }
                }
            }

            item {
                SectionTitle("Nome de ${editing.displayName}")
                // Seeded once per profile; persists on every keystroke.
                var name by remember(editing) { mutableStateOf(state.settings.rawChildName(editing)) }
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it.take(20)
                        onSetChildName(editing, name)
                    },
                    label = { Text("Nome da criança") },
                    placeholder = { Text(editing.displayName) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )
            }

            item {
                SectionTitle("Segurança")
                OutlinedButton(
                    onClick = { showPinDialog = true },
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) {
                    Icon(Icons.Rounded.Password, contentDescription = null)
                    Text(
                        text = "  Alterar PIN dos pais",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            item {
                SectionTitle("Tempo de tela de ${editing.displayName}")
                Text(
                    text = "Limite diário. Ao acabar, aparece a tela \"Acabou o tempo\" (destravada só com PIN).",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
                val currentLimit = state.settings.timeLimitMinutes(editing)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AppSettings.TIME_LIMIT_OPTIONS.forEach { minutes ->
                        FilterChip(
                            selected = minutes == currentLimit,
                            onClick = { onSetTimeLimit(editing, minutes) },
                            label = {
                                Text(if (minutes == 0) "Sem limite" else "$minutes min")
                            },
                        )
                    }
                }
            }

            item {
                SectionTitle("Apps liberados para ${editing.displayName}")
                Text(
                    text = "Ative os apps que ${editing.displayName} pode abrir.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
                HorizontalDivider()
            }

            items(state.allApps, key = { it.packageName }) { app ->
                val bitmap = remember(app.packageName) { app.icon.toBitmap().asImageBitmap() }
                ListItem(
                    leadingContent = {
                        Image(
                            bitmap = bitmap,
                            contentDescription = null,
                            modifier = Modifier.size(44.dp),
                        )
                    },
                    headlineContent = {
                        Text(app.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    supportingContent = {
                        Text(
                            app.packageName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    trailingContent = {
                        Switch(
                            checked = app.packageName in allowed,
                            onCheckedChange = { onToggleApp(editing, app.packageName, it) },
                        )
                    },
                )
                HorizontalDivider()
            }
        }
    }

    if (showPinDialog) {
        ChangePinDialog(
            onConfirm = { newPin ->
                onChangePin(newPin)
                showPinDialog = false
            },
            onDismiss = { showPinDialog = false },
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
    )
}
