package dev.jvfl.progtv.ui.screens.splash

import android.os.Build
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import dev.jvfl.progtv.ui.components.RadiusXs
import dev.jvfl.progtv.ui.theme.BgBlack
import dev.jvfl.progtv.ui.theme.BrandBlue
import dev.jvfl.progtv.ui.theme.BrandSoft
import dev.jvfl.progtv.ui.theme.TextPrimary
import kotlinx.coroutines.delay

/**
 * 2-second animated splash: the two-tone "ProgTV" wordmark scales/fades in over a soft
 * radial glow, with a flat progress underline that fills during the hold.
 *
 * Real blur is only used on API >= 31; on API 26–30 the glow layer degrades to an
 * alpha-only colour halo (no [Modifier.blur] call).
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(2000)
        onFinished()
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(700, easing = EaseOutCubic),
        label = "alpha",
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.86f,
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "scale",
    )
    // Progress underline width: animates 0 -> 120dp across the 2s hold.
    val underline by animateFloatAsState(
        targetValue = if (visible) 120f else 0f,
        animationSpec = tween(1600, easing = EaseOutCubic),
        label = "underline",
    )

    // Two-tone wordmark: "Prog" primary, "TV" brand-soft.
    val wordmark: AnnotatedString = buildAnnotatedString {
        withStyle(SpanStyle(color = TextPrimary)) { append("Prog") }
        withStyle(SpanStyle(color = BrandSoft)) { append("TV") }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(BrandBlue.copy(alpha = 0.14f), BgBlack),
                    radius = 1200f,
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Soft glow copy behind the wordmark — real blur only on API >= 31,
                // otherwise an alpha-only halo so it degrades without a blur call.
                val glowModifier =
                    if (Build.VERSION.SDK_INT >= 31) {
                        Modifier.scale(scale).blur(28.dp)
                    } else {
                        Modifier.scale(scale)
                    }
                Text(
                    text = wordmark,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    textAlign = TextAlign.Center,
                    color = BrandSoft.copy(
                        alpha = if (Build.VERSION.SDK_INT >= 31) alpha * 0.6f else alpha * 0.5f,
                    ),
                    modifier = glowModifier,
                )
                Text(
                    text = wordmark,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    textAlign = TextAlign.Center,
                    // Whole-wordmark fade via graphicsLayer to preserve the two-tone colours.
                    modifier = Modifier.graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    },
                )
            }
            // Flat, rectangular progress underline.
            Box(
                modifier = Modifier
                    .width(underline.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(RadiusXs))
                    .background(BrandBlue.copy(alpha = 0.4f)),
            )
        }
    }
}
