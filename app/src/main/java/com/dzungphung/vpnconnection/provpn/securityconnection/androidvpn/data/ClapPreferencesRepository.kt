package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.DetectionMode
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.FlashMode
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.SensitivityLevel
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.VibrationMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClapPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("clap_preferences", Context.MODE_PRIVATE)

    private val _isDetectionActive = MutableStateFlow(prefs.getBoolean("is_active", false))
    val isDetectionActive: StateFlow<Boolean> = _isDetectionActive.asStateFlow()

    private val _detectionMode = MutableStateFlow(
        DetectionMode.valueOf(prefs.getString("mode", DetectionMode.CLAP_AND_WHISTLE.name) ?: DetectionMode.CLAP_AND_WHISTLE.name)
    )
    val detectionMode: StateFlow<DetectionMode> = _detectionMode.asStateFlow()

    private val _sensitivity = MutableStateFlow(
        SensitivityLevel.valueOf(prefs.getString("sensitivity", SensitivityLevel.MEDIUM.name) ?: SensitivityLevel.MEDIUM.name)
    )
    val sensitivity: StateFlow<SensitivityLevel> = _sensitivity.asStateFlow()

    private val _selectedSoundId = MutableStateFlow(
        prefs.getString("sound_id", "police_siren") ?: "police_siren"
    )
    val selectedSoundId: StateFlow<String> = _selectedSoundId.asStateFlow()

    private val _flashMode = MutableStateFlow(
        FlashMode.valueOf(prefs.getString("flash_mode", FlashMode.FAST_STROBE.name) ?: FlashMode.FAST_STROBE.name)
    )
    val flashMode: StateFlow<FlashMode> = _flashMode.asStateFlow()

    private val _vibrationMode = MutableStateFlow(
        VibrationMode.valueOf(prefs.getString("vibration_mode", VibrationMode.STRONG.name) ?: VibrationMode.STRONG.name)
    )
    val vibrationMode: StateFlow<VibrationMode> = _vibrationMode.asStateFlow()

    private val _alarmDurationSec = MutableStateFlow(prefs.getInt("alarm_duration", 30))
    val alarmDurationSec: StateFlow<Int> = _alarmDurationSec.asStateFlow()

    private val _volumePercent = MutableStateFlow(prefs.getInt("volume_percent", 100))
    val volumePercent: StateFlow<Int> = _volumePercent.asStateFlow()

    private val _onlyWhenScreenOff = MutableStateFlow(prefs.getBoolean("only_screen_off", true))
    val onlyWhenScreenOff: StateFlow<Boolean> = _onlyWhenScreenOff.asStateFlow()

    fun setDetectionActive(active: Boolean) {
        _isDetectionActive.value = active
        prefs.edit { putBoolean("is_active", active) }
    }

    fun setDetectionMode(mode: DetectionMode) {
        _detectionMode.value = mode
        prefs.edit { putString("mode", mode.name) }
    }

    fun setSensitivity(level: SensitivityLevel) {
        _sensitivity.value = level
        prefs.edit { putString("sensitivity", level.name) }
    }

    fun setSelectedSoundId(id: String) {
        _selectedSoundId.value = id
        prefs.edit { putString("sound_id", id) }
    }

    fun setFlashMode(mode: FlashMode) {
        _flashMode.value = mode
        prefs.edit { putString("flash_mode", mode.name) }
    }

    fun setVibrationMode(mode: VibrationMode) {
        _vibrationMode.value = mode
        prefs.edit { putString("vibration_mode", mode.name) }
    }

    fun setAlarmDurationSec(sec: Int) {
        _alarmDurationSec.value = sec
        prefs.edit { putInt("alarm_duration", sec) }
    }

    fun setVolumePercent(percent: Int) {
        _volumePercent.value = percent
        prefs.edit { putInt("volume_percent", percent) }
    }

    fun setOnlyWhenScreenOff(onlyOff: Boolean) {
        _onlyWhenScreenOff.value = onlyOff
        prefs.edit { putBoolean("only_screen_off", onlyOff) }
    }
}

