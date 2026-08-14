package dev.jvfl.progtv.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val ProgTvColors = darkColorScheme(
    primary = BrandBlue,
    onPrimary = TextPrimary,
    secondary = BrandSoft,
    background = BgBlack,
    onBackground = TextPrimary,
    surface = BgSoft,
    onSurface = TextPrimary,
    surfaceVariant = GlassFill,
    outline = GlassStroke,
    error = ErrorRed,
)

// Clean hierarchy with explicit letterSpacing per the visual spec.
private val ProgTvTypography = Typography(
    displaySmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 44.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp, letterSpacing = (-0.25).sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, letterSpacing = 0.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, letterSpacing = 0.1.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, letterSpacing = 0.1.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.sp, letterSpacing = 0.1.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.8.sp),
)

@Composable
fun ProgTvTheme(
    // The app is dark-only by design, but honor the system flag for completeness.
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ProgTvColors,
        typography = ProgTvTypography,
        content = content,
    )
}
