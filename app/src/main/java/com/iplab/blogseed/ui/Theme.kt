package com.iplab.blogseed.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/*
 * 디자인 컨셉: 에디토리얼(원고지).
 *
 * 이 앱에서 사용자가 오래 들여다보는 것은 버튼이 아니라 자기가 쓴 글이다.
 * 그래서 세 가지를 규칙으로 삼는다.
 *   1) 바탕은 순백이 아니라 살짝 누런 종이색. 장시간 읽어도 눈이 덜 시리다.
 *   2) 글 본문은 세리프에 넓은 행간. UI 라벨만 산세리프로 두어 "읽을 것"과 "누를 것"을 분리한다.
 *   3) 강조색은 잉크 계열 딥그린 하나. 원고 위에 색이 튀지 않게 한다.
 *
 * dynamicColor(Material You)는 일부러 쓰지 않는다. 기기 배경화면에 따라
 * 종이색과 잉크색이 바뀌면 위 컨셉이 통째로 무너지기 때문이다.
 */

private val PaperLight = lightColorScheme(
    primary = Color(0xFF2F5D4B),          // 잉크 그린
    onPrimary = Color(0xFFFBF8F1),
    primaryContainer = Color(0xFFD8E7DE),
    onPrimaryContainer = Color(0xFF17342A),
    secondary = Color(0xFF8A6A45),        // 갈색 주석
    onSecondary = Color(0xFFFBF8F1),
    secondaryContainer = Color(0xFFEDE0CE),
    onSecondaryContainer = Color(0xFF3D2D19),
    background = Color(0xFFFBF8F1),       // 종이
    onBackground = Color(0xFF1F1D18),
    surface = Color(0xFFFFFDF8),
    onSurface = Color(0xFF1F1D18),
    surfaceVariant = Color(0xFFF0EADC),
    onSurfaceVariant = Color(0xFF6B6555),
    outline = Color(0xFFCFC7B4),
    outlineVariant = Color(0xFFE4DDCC),
    error = Color(0xFF9C4234),
    onError = Color(0xFFFBF8F1)
)

private val PaperDark = darkColorScheme(
    primary = Color(0xFF9CCDB6),
    onPrimary = Color(0xFF12291F),
    primaryContainer = Color(0xFF23402F),
    onPrimaryContainer = Color(0xFFD8E7DE),
    secondary = Color(0xFFD9BC95),
    onSecondary = Color(0xFF2C1F10),
    secondaryContainer = Color(0xFF3E2F1D),
    onSecondaryContainer = Color(0xFFEDE0CE),
    background = Color(0xFF13120F),       // 불 끈 책상
    onBackground = Color(0xFFEDE9DE),
    surface = Color(0xFF1B1A16),
    onSurface = Color(0xFFEDE9DE),
    surfaceVariant = Color(0xFF2A2822),
    onSurfaceVariant = Color(0xFF9C9484),
    outline = Color(0xFF433F36),
    outlineVariant = Color(0xFF2F2C26),
    error = Color(0xFFE4948A),
    onError = Color(0xFF2A0F0A)
)

/** 원고는 세리프, 조작부는 산세리프. */
private val EditorialType = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 25.sp,
        lineHeight = 33.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 29.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 24.sp
    ),
    // 본문. 행간을 글자크기의 1.65배로 잡아 긴 한글 문단이 답답하지 않게 한다.
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 17.sp,
        lineHeight = 28.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 15.sp,
        lineHeight = 25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.02.em
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.04.em
    ),
    // 섹션 머리표. 자간을 벌려 본문과 확실히 다른 층위로 읽히게 한다.
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 0.16.em
    )
)

/** 종이를 오려 붙인 느낌이라 모서리는 거의 굴리지 않는다. */
private val PaperShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(12.dp)
)

@Composable
fun BlogSeedTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) PaperDark else PaperLight,
        typography = EditorialType,
        shapes = PaperShapes,
        content = content
    )
}
