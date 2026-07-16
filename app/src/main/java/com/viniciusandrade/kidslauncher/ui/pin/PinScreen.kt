package com.viniciusandrade.kidslauncher.ui.pin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable

private const val PIN_LENGTH = 4

/**
 * Numeric PIN gate shown before any parental action. [onVerify] returns true
 * when the entered PIN is correct; the screen handles clearing and error state.
 */
@Composable
fun PinScreen(
    title: String,
    onVerify: (String) -> Boolean,
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    fun submit(candidate: String) {
        if (onVerify(candidate)) {
            onSuccess()
        } else {
            error = true
            pin = ""
        }
    }

    fun append(digit: String) {
        if (pin.length >= PIN_LENGTH) return
        error = false
        pin += digit
        if (pin.length == PIN_LENGTH) submit(pin)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))

            PinDots(filled = pin.length, error = error)

            if (error) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "PIN incorreto, tente de novo",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            Spacer(Modifier.height(32.dp))

            Keypad(
                onDigit = ::append,
                onBackspace = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
            )

            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onCancel) {
                Text("Voltar", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun PinDots(filled: Int, error: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(PIN_LENGTH) { index ->
            val active = index < filled
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .then(
                        if (active) Modifier.background(
                            if (error) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        )
                        else Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    )
            )
        }
    }
}

@Composable
private fun Keypad(onDigit: (String) -> Unit, onBackspace: () -> Unit) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "<"),
    )
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { key ->
                    when (key) {
                        "" -> Spacer(Modifier.size(76.dp))
                        "<" -> KeypadKey(onClick = onBackspace) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                contentDescription = "Apagar",
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        else -> KeypadKey(onClick = { onDigit(key) }) {
                            Text(
                                text = key,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadKey(onClick: () -> Unit, content: @Composable () -> Unit) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center) { content() }
    }
}
