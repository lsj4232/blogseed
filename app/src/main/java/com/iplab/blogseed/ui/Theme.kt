package com.iplab.blogseed.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Visual language: nocturnal creative dashboard.
 * Near-black green canvas, translucent-looking raised panels, vivid mint actions
 * and violet/cyan accents. The structure remains BlogSeed's own writing workflow.
 */
val SeedMint = Color(0xFF22FFA6)
val SeedViolet = Color(0xFF9556F8)
val SeedCyan = Color(0xFF1DE7E0)
val SeedCanvas = Color(0xFF060B0A)
val SeedPanel = Color(0xFF101A17)
val SeedPanelHigh = Color(0xFF17241F)

private val SeedDark = darkColorScheme(
    primary = SeedMint,
    onPrimary = Color(0xFF002114),
    primaryContainer = Color(0xFF123D2D),
    onPrimaryContainer = Color(0xFFB8FFDA),
    secondary = SeedViolet,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF332153),
    onSecondaryContainer = Color(0xFFE7D8FF),
    tertiary = SeedCyan,
    onTertiary = Color(0xFF00201F),
    tertiaryContainer = Color(0xFF0B4544),
    onTertiaryContainer = Color(0xFFAAFFFC),
    background = SeedCanvas,
    onBackground = Color(0xFFF4F7F5),
    surface = SeedPanel,
    onSurface = Color(0xFFF4F7F5),
    surfaceVariant = SeedPanelHigh,
    onSurfaceVariant = Color(0xFFA9B8B1),
    outline = Color(0xFF385047),
    outlineVariant = Color(0xFF21342D),
    error = Color(0xFFFF6F7D),
    onError = Color(0xFF300008),
    errorContainer = Color(0xFF4A1820),
    onErrorContainer = Color(0xFFFFDADD)
)

private val DashboardType = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.8).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 27.sp,
        lineHeight = 34.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 29.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 27.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 25.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 12.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 1.2.sp
    )
)

private val DashboardShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun BlogSeedTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SeedDark,
        typography = DashboardType,
        shapes = DashboardShapes,
        content = content
    )
}
