package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model

import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.R

enum class DetectionMode(val displayNameRes: Int, val descRes: Int) {
    CLAP_ONLY(R.string.mode_clap_only, R.string.mode_clap_desc),
    WHISTLE_ONLY(R.string.mode_whistle_only, R.string.mode_whistle_desc),
    CLAP_AND_WHISTLE(R.string.mode_clap_and_whistle, R.string.mode_both_desc)
}

enum class SensitivityLevel(val displayNameRes: Int, val thresholdScale: Float) {
    LOW(R.string.sensitivity_low, 1.3f),
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

enum class SoundCategory(val titleRes: Int) {
    EMERGENCY(R.string.cat_emergency),
    SIGNALS(R.string.cat_signals),
    PLAYFUL(R.string.cat_playful)
}

data class SoundItem(
    val id: String,
    val nameRes: Int,
    val soundResId: Int,
    val isVip: Boolean = false,
    val category: SoundCategory = SoundCategory.EMERGENCY,
    val iconEmoji: String = ""
)

object AvailableSounds {
    val list = listOf(
        SoundItem("police_siren", R.string.sound_police_siren, R.raw.police_siren, isVip = false, category = SoundCategory.EMERGENCY),
        SoundItem("emergency_alarm", R.string.sound_emergency_alarm, R.raw.emergency_alarm, isVip = false, category = SoundCategory.EMERGENCY),
        SoundItem("air_horn", R.string.sound_air_horn, R.raw.air_horn, isVip = false, category = SoundCategory.SIGNALS),
        SoundItem("whistle_sound", R.string.sound_whistle, R.raw.whistle_sound, isVip = true, category = SoundCategory.SIGNALS),
        SoundItem("cat_meow", R.string.sound_cat_meow, R.raw.cat_meow, isVip = true, category = SoundCategory.PLAYFUL),
        SoundItem("dog_bark", R.string.sound_dog_bark, R.raw.dog_bark, isVip = true, category = SoundCategory.PLAYFUL)
    )

    fun getById(id: String): SoundItem {
        return list.find { it.id == id } ?: list.first()
    }
}
