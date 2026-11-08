package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class SoundVisual(
    val icon: ImageVector,
    val iconTint: Color,
    val bgTint: Color
)

object SoundVisuals {
    fun getVisual(soundId: String): SoundVisual {
        return when (soundId) {
            "police_siren" -> SoundVisual(
                icon = Icons.Default.Shield,
                iconTint = Color(0xFF60A5FA),
                bgTint = Color(0x263B82F6)
            )
            "emergency_alarm" -> SoundVisual(
                icon = Icons.Default.NotificationsActive,
                iconTint = Color(0xFFF87171),
                bgTint = Color(0x26EF4444)
            )
            "air_horn" -> SoundVisual(
                icon = Icons.Default.Campaign,
                iconTint = Color(0xFFFB923C),
                bgTint = Color(0x26F97316)
            )
            "whistle_sound" -> SoundVisual(
                icon = Icons.Default.GraphicEq,
                iconTint = Color(0xFFA78BFA),
                bgTint = Color(0x268B5CF6)
            )
            "cat_meow" -> SoundVisual(
                icon = Icons.Default.Pets,
                iconTint = Color(0xFFF472B6),
                bgTint = Color(0x26EC4899)
            )
            "dog_bark" -> SoundVisual(
                icon = Icons.Default.Pets,
                iconTint = Color(0xFF34D399),
                bgTint = Color(0x2610B981)
            )
            else -> SoundVisual(
                icon = Icons.Default.VolumeUp,
                iconTint = Color(0xFF9CA3AF),
                bgTint = Color(0x264B5563)
            )
        }
    }
}

