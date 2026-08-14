package dev.jvfl.progtv.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.jvfl.progtv.ui.theme.BrandSoft
import dev.jvfl.progtv.ui.theme.FocusGlow
import dev.jvfl.progtv.ui.theme.FocusStroke
import dev.jvfl.progtv.ui.theme.GlassFill
import dev.jvfl.progtv.ui.theme.GlassFillFocus
import dev.jvfl.progtv.ui.theme.GlassStroke

// ---------------------------------------------------------------------------
// Corner radius scale (FLAT / RECTANGULAR — never exceed 12dp anywhere).
// ---------------------------------------------------------------------------
val RadiusXs: Dp = 6.dp // logo thumb inner, small chips / badges
val RadiusSm: Dp = 8.dp // channel rows, category rows, buttons, text field, logo tile
val RadiusMd: Dp = 10.dp // panels / cards, list header
val RadiusLg: Dp = 12.dp // large modal panels (settings / error) MAX

// ---------------------------------------------------------------------------
// Glass surface modifier.
// ---------------------------------------------------------------------------

/**
 * Applies the app's translucent "glass" panel look (fill + hairline stroke).
 * Default radius is flat 10dp; pass [fill]/[stroke] for stronger rail/modal panels.
 */
fun Modifier.glass(
    radius: Dp = RadiusMd,
    fill: Color = GlassFill,
    stroke: Color = GlassStroke,
): Modifier =
    clip(RoundedCornerShape(radius))
        .background(fill)
        .border(1.dp, stroke, RoundedCornerShape(radius))

/**
 * Soft blue focus glow implemented as a shadow (we avoid elevation shadows elsewhere).
 * Apply BEFORE clip/background. No-op when [focused] is false.
 */
fun Modifier.focusGlow(
    focused: Boolean,
    shape: Shape,
    elevation: Dp = 10.dp,
): Modifier =
    if (focused) {
        this.shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = FocusGlow,
            spotColor = FocusGlow,
        )
    } else {
        this
    }

// ---------------------------------------------------------------------------
// Reusable FLAT / RECTANGULAR focusable surface.
// ---------------------------------------------------------------------------

/**
 * A D-pad-friendly flat rectangular surface that carries the full §2 focus recipe:
 * fill brighten + blue border + in-place scale + soft glow. Suitable as the base
 * for rows, tiles and cards across screens.
 *
 * Focus is carried by fill + border (works with reduced motion); scale is additive.
 * Modifier order matches the spec: graphicsLayer(scale) -> glow -> clip -> background
 * -> border -> onFocusChanged -> clickable -> padding.
 */
@Composable
fun FocusableSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(RadiusSm),
    scaleTarget: Float = 1.03f,
    restFill: Color = GlassFill,
    focusFill: Color = GlassFillFocus,
    restStroke: Color = GlassStroke,
    focusStrokeColor: Color = FocusStroke,
    focusStrokeWidth: Dp = 1.5.dp,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    focusRequester: FocusRequester? = null,
    interactionSource: MutableInteractionSource? = null,
    onFocusedChanged: (Boolean) -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val scale = focusScale(focused, target = scaleTarget)

    var chain = modifier
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .focusGlow(focused, shape)
        .clip(shape)
        .background(if (focused) focusFill else restFill)
        .border(
            width = if (focused) focusStrokeWidth else 1.dp,
            color = if (focused) focusStrokeColor else restStroke,
            shape = shape,
        )
    if (focusRequester != null) chain = chain.focusRequester(focusRequester)
    chain = chain
        .onFocusChanged {
            focused = it.isFocused
            onFocusedChanged(it.isFocused)
        }
        .clickable(interactionSource = interactionSource, indication = null) { onClick() }
        .padding(contentPadding)

    Box(modifier = chain, content = content)
}

// ---------------------------------------------------------------------------
// Spinner / buffering.
// ---------------------------------------------------------------------------

/** A lightweight indeterminate spinner drawn with Canvas (theme-agnostic). */
@Composable
fun Spinner(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = BrandSoft,
    strokeWidth: Dp = 4.dp,
) {
    val transition = rememberInfiniteTransition(label = "spinner")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "angle",
    )
    Canvas(modifier = modifier.size(size)) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        drawArc(
            color = color.copy(alpha = 0.14f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = stroke,
        )
        rotate(angle) {
            drawArc(
                color = color,
                startAngle = 0f,
                sweepAngle = 90f,
                useCenter = false,
                style = stroke,
            )
        }
    }
}

/**
 * Spinner + download rate (KB/s). [compact] renders a small glass chip for the
 * mid-play stall hint; non-compact returns the bare spinner+rate row for callers
 * that wrap it in their own glass card.
 */
@Composable
fun BufferingIndicator(
    kbps: Long,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Row(
        modifier = if (compact) {
            modifier
                .glass(RadiusSm, fill = dev.jvfl.progtv.ui.theme.GlassFillStrong, stroke = GlassStroke)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        } else {
            modifier
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp),
    ) {
        Spinner(
            size = if (compact) 18.dp else 48.dp,
            strokeWidth = if (compact) 2.5.dp else 4.dp,
        )
        Text(
            text = "${formatKbps(kbps)} KB/s",
            style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.titleMedium,
            color = BrandSoft,
        )
    }
}

/**
 * Animated focus scale factor for cards/tiles reacting to D-pad focus.
 * Callers pass 1.03 for rows/buttons and 1.02 for category rows.
 */
@Composable
fun focusScale(focused: Boolean, target: Float = 1.03f): Float {
    val scale by animateFloatAsState(
        targetValue = if (focused) target else 1f,
        animationSpec = tween(140),
        label = "focusScale",
    )
    return scale
}

/** Formats a bytes/sec rate into whole KB/s. */
fun formatKbps(bytesPerSec: Long): String {
    val kb = bytesPerSec / 1024
    return kb.toString()
}
