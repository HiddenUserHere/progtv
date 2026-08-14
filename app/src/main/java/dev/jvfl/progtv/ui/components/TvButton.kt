package dev.jvfl.progtv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import dev.jvfl.progtv.ui.theme.BrandBlue
import dev.jvfl.progtv.ui.theme.BrandDeep
import dev.jvfl.progtv.ui.theme.FocusStroke
import dev.jvfl.progtv.ui.theme.GlassFill
import dev.jvfl.progtv.ui.theme.GlassStroke
import dev.jvfl.progtv.ui.theme.TextPrimary

/** A D-pad-friendly flat button that brightens, scales and glows on focus. */
@Composable
fun TvButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
) {
    var focused by remember { mutableStateOf(false) }
    val scale = focusScale(focused, target = 1.03f)
    val shape = RoundedCornerShape(RadiusSm)
    val bg = when {
        focused -> BrandBlue
        primary -> BrandDeep
        else -> GlassFill
    }
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .focusGlow(focused, shape)
            .clip(shape)
            .background(bg)
            .border(
                width = if (focused) 1.5.dp else 1.dp,
                color = if (focused) FocusStroke else GlassStroke,
                shape = shape,
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable { onClick() }
            .defaultMinSize(minHeight = 48.dp)
            .padding(horizontal = 22.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = if (focused) Color.White else TextPrimary,
        )
    }
}
