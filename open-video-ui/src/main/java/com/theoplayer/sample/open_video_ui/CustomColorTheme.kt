package com.theoplayer.sample.open_video_ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

enum class ColorPreset(
    val label: String,
    val accentColor: Color,
    val fullTheme: Boolean,
    val scheme: ColorScheme
) {
    ORANGE(
        label = "Orange",
        accentColor = Color(0xFFFF6D00),
        fullTheme = false,
        scheme = darkColorScheme(
            primary = Color(0xFFFF6D00),
            onPrimary = Color.White,
            primaryContainer = Color(0xFF803600),
            onPrimaryContainer = Color(0xFFFF6D00),
            secondary = Color(0xFFFFAB40),
            onSecondary = Color.Black,
            background = Color(0xFF1A1110),
            onBackground = Color(0xFFEDE0DB),
            surface = Color(0xFF1A1110),
            onSurface = Color(0xFFEDE0DB),
            surfaceVariant = Color(0xFF52443D),
            onSurfaceVariant = Color(0xFFD7C2B9),
        )
    ),
    TEAL(
        label = "Teal",
        accentColor = Color(0xFF00BFA5),
        fullTheme = false,
        scheme = darkColorScheme(
            primary = Color(0xFF00BFA5),
            onPrimary = Color.White,
            primaryContainer = Color(0xFF005B4F),
            onPrimaryContainer = Color(0xFFA7F3EC),
            secondary = Color(0xFF4DD0E1),
            onSecondary = Color.Black,
            background = Color(0xFF0F1A19),
            onBackground = Color(0xFFDAE5E3),
            surface = Color(0xFF0F1A19),
            onSurface = Color(0xFFDAE5E3),
            surfaceVariant = Color(0xFF3B4F4C),
            onSurfaceVariant = Color(0xFFBCC9C6),
        )
    ),
    PINK(
        label = "Pink",
        accentColor = Color(0xFFFF4081),
        fullTheme = false,
        scheme = darkColorScheme(
            primary = Color(0xFFFF4081),
            onPrimary = Color.White,
            primaryContainer = Color(0xFF8C0032),
            onPrimaryContainer = Color(0xFFFFD9E2),
            secondary = Color(0xFFFF80AB),
            onSecondary = Color.Black,
            background = Color(0xFF1A1014),
            onBackground = Color(0xFFEDDDE3),
            surface = Color(0xFF1A1014),
            onSurface = Color(0xFFEDDDE3),
            surfaceVariant = Color(0xFF524047),
            onSurfaceVariant = Color(0xFFD8C1C9),
        )
    ),
    GREEN(
        label = "Green",
        accentColor = Color(0xFF76FF03),
        fullTheme = true,
        scheme = darkColorScheme(
            primary = Color(0xFF76FF03),
            onPrimary = Color.Black,
            primaryContainer = Color(0xFF2E7D00),
            onPrimaryContainer = Color(0xFFC8FFB0),
            secondary = Color(0xFFB2FF59),
            onSecondary = Color.Black,
            background = Color(0xFF111A0F),
            onBackground = Color(0xFF76FF03),
            surface = Color(0xFF111A0F),
            onSurface = Color(0xFFDDE6D8),
            surfaceVariant = Color(0xFF3E4F3B),
            onSurfaceVariant = Color(0xFFBFC9BA),
        )
    ),
    RED(
        label = "Red",
        accentColor = Color(0xFFFF1744),
        fullTheme = true,
        scheme = darkColorScheme(
            primary = Color(0xFFFF1744),
            onPrimary = Color.White,
            primaryContainer = Color(0xFF930023),
            onPrimaryContainer = Color(0xFFFFDAD9),
            secondary = Color(0xFFFF5252),
            onSecondary = Color.Black,
            background = Color(0xFF1A0F0F),
            onBackground = Color(0xFFFF1744),
            surface = Color(0xFF1A0F0F),
            onSurface = Color(0xFFEDDBDB),
            surfaceVariant = Color(0xFF524040),
            onSurfaceVariant = Color(0xFFD8C1C1),
        )
    ),
    PURPLE(
        label = "Purple",
        accentColor = Color(0xFFBB86FC),
        fullTheme = true,
        scheme = darkColorScheme(
            primary = Color(0xFFBB86FC),
            onPrimary = Color.Black,
            primaryContainer = Color(0xFF6200EE),
            onPrimaryContainer = Color(0xFFE8DEFF),
            secondary = Color(0xFF03DAC6),
            onSecondary = Color.Black,
            background = Color(0xFF12101A),
            onBackground = Color(0xFFBB86FC),
            surface = Color(0xFF12101A),
            onSurface = Color(0xFFE0DBE8),
            surfaceVariant = Color(0xFF464052),
            onSurfaceVariant = Color(0xFFC9C1D8),
        )
    );
}
