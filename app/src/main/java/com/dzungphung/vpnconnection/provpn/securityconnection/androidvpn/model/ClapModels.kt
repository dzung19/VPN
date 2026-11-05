package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model

import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.R

enum class DetectionMode(val displayNameRes: Int) {
    CLAP_ONLY(R.string.mode_clap_only),
    WHISTLE_ONLY(R.string.mode_whistle_only),
    CLAP_AND_WHISTLE(R.string.mode_clap_and_whistle)
}

enum class SensitivityLevel(val displayNameRes: Int, val thresholdScale: Float) {
    LOW(R.string.sensitivity_low, 1.4f),
    MEDIUM(R.string.sensitivity_medium, 1.0f),
    HIGH(R.string.sensitivity_high, 0.7f)
}

enum class FlashMode(val displayNameRes: Int) {
    OFF(R.string.flash_off),
    FAST_STROBE(R.string.flash_strobe),
    SOS(R.string.flash_sos),
    CONTINUOUS(R.string.flash_continuous)
}

enum class VibrationMode(val displayNameRes: Int) {
    OFF(R.string.vibration_off),
    STRONG(R.string.vibration_strong),
    HEARTBEAT(R.string.vibration_heartbeat)
}

data class SoundItem(
    val id: String,
    val nameRes: Int,
    val soundResId: Int,
    val isVip: Boolean = false,
    val iconEmoji: String
)

object AvailableSounds {
    val list = listOf(
        SoundItem("police_siren", R.string.sound_police_siren, R.raw.police_siren, isVip = false, iconEmoji = "🚨"),
        SoundItem("emergency_alarm", R.string.sound_emergency_alarm, R.raw.emergency_alarm, isVip = false, iconEmoji = "⏰"),
        SoundItem("air_horn", R.string.sound_air_horn, R.raw.air_horn, isVip = false, iconEmoji = "📯"),
        SoundItem("whistle_sound", R.string.sound_whistle, R.raw.whistle_sound, isVip = true, iconEmoji = "🎵"),
        SoundItem("cat_meow", R.string.sound_cat_meow, R.raw.cat_meow, isVip = true, iconEmoji = "🐱"),
        SoundItem("dog_bark", R.string.sound_dog_bark, R.raw.dog_bark, isVip = true, iconEmoji = "🐶")
    )

    fun getById(id: String): SoundItem {
        return list.find { it.id == id } ?: list.first()
    }
}

