package dev.jvfl.progtv.ui.screens.error

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.jvfl.progtv.ui.components.TvButton
import dev.jvfl.progtv.ui.components.glass
import dev.jvfl.progtv.ui.theme.ErrorRed
import dev.jvfl.progtv.ui.theme.Scrim
import dev.jvfl.progtv.ui.theme.TextMuted
import dev.jvfl.progtv.ui.theme.TextPrimary

/** Full-screen error panel shown when the catalog cannot be loaded (e.g. backend down). */
@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Scrim)
            .focusable(), // absorb stray D-pad input behind the panel
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.glass(24.dp).widthIn(max = 560.dp).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.CloudOff,
                contentDescription = null,
                tint = ErrorRed,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            Text(
                text = "Backend indisponível",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center,
            )
            TvButton(text = "Tentar novamente", onClick = onRetry, primary = true)
        }
    }
}
