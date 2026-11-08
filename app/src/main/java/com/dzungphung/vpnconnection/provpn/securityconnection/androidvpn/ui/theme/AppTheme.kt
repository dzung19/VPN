package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColors {
    val Background = Color(0xFF0D0F14)
    val Surface = Color(0xFF151821)
    val SurfaceVariant = Color(0xFF1D222F)
    val SurfaceBorder = Color(0xFF282F40)

    val AccentCoral = Color(0xFFFF5722)
    val AccentCoralSoft = Color(0x33FF5722)

    val AccentEmerald = Color(0xFF10B981)
    val AccentEmeraldSoft = Color(0x3310B981)

    val AccentBlue = Color(0xFF3B82F6)
    val AccentBlueSoft = Color(0x333B82F6)

    val AccentAmber = Color(0xFFF59E0B)
    val AccentAmberSoft = Color(0x33F59E0B)

    val AccentRed = Color(0xFFEF4444)
    val AccentRedSoft = Color(0x33EF4444)

    val TextPrimary = Color(0xFFF9FAFB)
    val TextSecondary = Color(0xFF9CA3AF)
    val TextMuted = Color(0xFF6B7280)
}

val AppDarkColorScheme = darkColorScheme(
    background = AppColors.Background,
    surface = AppColors.Surface,
    surfaceVariant = AppColors.SurfaceVariant,
    onBackground = AppColors.TextPrimary,
    onSurface = AppColors.TextPrimary,
    onSurfaceVariant = AppColors.TextSecondary,
    primary = AppColors.AccentCoral,
    onPrimary = Color.White,
    secondary = AppColors.AccentEmerald,
    onSecondary = Color.Black
)

@Composable
fun ClapFinderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppDarkColorScheme,
        content = content
    )
}

