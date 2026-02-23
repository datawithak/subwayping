package com.subwayping.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.subwayping.app.R

// Subway-inspired dark theme colors
val SubwayBlack = Color(0xFF121212)
val SubwayDarkGray = Color(0xFF1E1E1E)
val SubwayMedGray = Color(0xFF2A2A2A)
val SubwayLightGray = Color(0xFFB0B0B0)
val SubwayWhite = Color(0xFFF5F5F5)
val SubwayRed = Color(0xFFEE352E)
val SubwayGreen = Color(0xFF00933C)
val SubwayYellow = Color(0xFFFCCC0A)
val PingRed = Color(0xFFFF3B30)
val StopGreen = Color(0xFF34C759)

// Excalifont — hand-drawn style
val Excalifont = FontFamily(
    Font(R.font.excalifont, FontWeight.Normal),
    Font(R.font.excalifont, FontWeight.Light),
    Font(R.font.excalifont, FontWeight.Medium),
    Font(R.font.excalifont, FontWeight.SemiBold),
    Font(R.font.excalifont, FontWeight.Bold),
    Font(R.font.excalifont, FontWeight.Black),
)

private val ExcalifontTypography = Typography(
    displayLarge = TextStyle(fontFamily = Excalifont, fontWeight = FontWeight.Bold, fontSize = 57.sp),
    displayMedium = TextStyle(fontFamily = Excalifont, fontWeight = FontWeight.Bold, fontSize = 45.sp),
    displaySmall = TextStyle(fontFamily = Excalifont, fontWeight = FontWeight.Bold, fontSize = 36.sp),
    headlineLarge = TextStyle(fontFamily = Excalifont, fontWeight = FontWeight.Bold, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = Excalifont, fontWeight = FontWeight.SemiBold, fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = Excalifont, fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = Excalifont, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = Excalifont, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    titleSmall = TextStyle(fontFamily = Excalifont, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    bodyLarge = TextStyle(fontFamily = Excalifont, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = Excalifont, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = Excalifont, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = Excalifont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = Excalifont, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = Excalifont, fontWeight = FontWeight.Medium, fontSize = 11.sp),
)

private val DarkColorScheme = darkColorScheme(
    primary = PingRed,
    secondary = SubwayYellow,
    tertiary = SubwayGreen,
    background = SubwayBlack,
    surface = SubwayDarkGray,
    surfaceVariant = SubwayMedGray,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = SubwayWhite,
    onSurface = SubwayWhite,
    onSurfaceVariant = SubwayLightGray,
    error = Color(0xFFFF6B6B),
)

@Composable
fun SubwayPingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = ExcalifontTypography,
        content = content
    )
}
