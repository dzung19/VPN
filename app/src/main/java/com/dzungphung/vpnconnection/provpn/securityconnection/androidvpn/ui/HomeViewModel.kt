package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.alert.AlarmController
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.BillingManager
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.ClapPreferencesRepository
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.*
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.service.ClapDetectionService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferencesRepository: ClapPreferencesRepository,
    private val alarmController: AlarmController,
    private val billingManager: BillingManager
) : ViewModel() {

    val isDetectionActive: StateFlow<Boolean> = preferencesRepository.isDetectionActive
    val isAlarmRinging: StateFlow<Boolean> = alarmController.isAlarmRinging
    val isPremium: StateFlow<Boolean> = billingManager.isPremium
    val detectionMode: StateFlow<DetectionMode> = preferencesRepository.detectionMode
    val sensitivity: StateFlow<SensitivityLevel> = preferencesRepository.sensitivity
    val selectedSoundId: StateFlow<String> = preferencesRepository.selectedSoundId
    val flashMode: StateFlow<FlashMode> = preferencesRepository.flashMode
    val vibrationMode: StateFlow<VibrationMode> = preferencesRepository.vibrationMode
    val onlyWhenScreenOff: StateFlow<Boolean> = preferencesRepository.onlyWhenScreenOff

    val formattedPrice: String
        get() = billingManager.getFormattedPrice()

    fun toggleDetection(context: Context) {
        val current = isDetectionActive.value
        if (current) {
            ClapDetectionService.stopService(context)
        } else {
            ClapDetectionService.startService(context)
        }
    }

    fun setDetectionMode(mode: DetectionMode) {
        preferencesRepository.setDetectionMode(mode)
    }

    fun setSensitivity(level: SensitivityLevel) {
        preferencesRepository.setSensitivity(level)
    }

    fun selectSound(soundItem: SoundItem) {
        preferencesRepository.setSelectedSoundId(soundItem.id)
    }

    fun setFlashMode(mode: FlashMode) {
        preferencesRepository.setFlashMode(mode)
    }

    fun setVibrationMode(mode: VibrationMode) {
        preferencesRepository.setVibrationMode(mode)
    }

    fun setOnlyWhenScreenOff(enabled: Boolean) {
        preferencesRepository.setOnlyWhenScreenOff(enabled)
    }

    fun testAlarm() {
        if (isAlarmRinging.value) {
            alarmController.stopAlarm()
        } else {
            val soundItem = AvailableSounds.getById(selectedSoundId.value)
            alarmController.startAlarm(
                soundResId = soundItem.soundResId,
                volumePercent = preferencesRepository.volumePercent.value,
                flashMode = preferencesRepository.flashMode.value,
                vibrationMode = preferencesRepository.vibrationMode.value,
                maxDurationSec = 15 // 15s test preview
            )
        }
    }

    fun stopAlarm() {
        alarmController.stopAlarm()
    }

    fun buyVip(activity: Activity) {
        billingManager.launchPurchaseFlow(activity)
    }
}
