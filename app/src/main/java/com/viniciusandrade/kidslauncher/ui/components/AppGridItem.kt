package com.viniciusandrade.kidslauncher.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.viniciusandrade.kidslauncher.data.model.LauncherApp

/**
 * A single big, tappable app tile. Icon size and label visibility are driven by
 * the active profile so the same component serves both the 4- and 10-year-old.
 */
@Composable
fun AppGridItem(
    app: LauncherApp,
    iconSize: Int,
    showLabel: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Convert the platform Drawable to an ImageBitmap once per app.
    val imageBitmap = remember(app.packageName) {
        app.icon.toBitmap().asImageBitmap()
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp,
            modifier = Modifier.size((iconSize + 20).dp),
        ) {
            Image(
                bitmap = imageBitmap,
                contentDescription = app.label,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .padding(10.dp)
                    .size(iconSize.dp),
            )
        }
        if (showLabel) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
