package com.viniciusandrade.kidslauncher.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

private const val PIN_LENGTH = 4

/**
 * Collects a new 4-digit PIN twice and only enables confirmation when both
 * entries match and have the correct length.
 */
@Composable
fun ChangePinDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var pin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    val valid = pin.length == PIN_LENGTH && pin == confirm
    val mismatch = confirm.isNotEmpty() && pin != confirm

    fun sanitize(value: String) = value.filter { it.isDigit() }.take(PIN_LENGTH)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Alterar PIN") },
        text = {
            Column {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = sanitize(it) },
                    label = { Text("Novo PIN (4 dígitos)") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = sanitize(it) },
                    label = { Text("Confirmar PIN") },
                    singleLine = true,
                    isError = mismatch,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                )
                if (mismatch) {
                    Spacer(Modifier.height(8.dp))
                    Text("Os PINs não coincidem.")
                }
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = { onConfirm(pin) }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        },
    )
}
