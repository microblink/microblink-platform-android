package com.microblink.platform.sample.customui.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.microblink.platform.engine.ScannerAppearance
import com.microblink.platform.engine.ScannerColors
import com.microblink.platform.engine.ScannerTypography

// This app's own look, so it plainly isn't the SDK's drop-in UI: green on warm paper, serif headings.
private val Forest = Color(0xFF1B5E20)
private val Mint = Color(0xFFE3F1E4)
private val Ink = Color(0xFF14261A)
private val Paper = Color(0xFFFBF8F1)

private val Colors = lightColorScheme(
    primary = Forest,
    onPrimary = Color.White,
    primaryContainer = Mint,
    onPrimaryContainer = Ink,
    secondary = Forest,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
)

private val Serif = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)

@Composable
fun CustomTheme(content: @Composable () -> Unit) {
    val typography = Typography()
    MaterialTheme(
        colorScheme = Colors,
        shapes = Shapes(small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(20.dp)),
        typography = typography.copy(
            headlineMedium = typography.headlineMedium.merge(Serif),
            titleSmall = typography.titleSmall.merge(Serif),
        ),
        content = content,
    )
}

/** Engine's scanner and face screens in this app's colors and heading font. */
val scannerAppearance = ScannerAppearance(
    colors = ScannerColors(
        primary = Forest.toArgb(),
        onPrimary = Color.White.toArgb(),
        surface = Paper.toArgb(),
        onSurface = Ink.toArgb(),
    ),
    typography = ScannerTypography(
        // A style replaces the scanner's whole default style, so each sets its size too.
        onboardingTitle = Serif.copy(fontSize = 22.sp, lineHeight = 28.sp),
        helpDialogTitle = Serif.copy(fontSize = 22.sp, lineHeight = 28.sp),
        errorDialogTitle = Serif.copy(fontSize = 22.sp, lineHeight = 28.sp),
    ),
)
