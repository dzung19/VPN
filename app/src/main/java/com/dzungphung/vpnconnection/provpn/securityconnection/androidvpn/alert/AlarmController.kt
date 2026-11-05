package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.alert

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.CombinedVibration
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.FlashMode
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.VibrationMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmController @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AlarmController"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _isAlarmRinging = MutableStateFlow(false)
    val isAlarmRinging: StateFlow<Boolean> = _isAlarmRinging.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private var flashJob: Job? = null
    private var autoStopJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val cameraId: String? by lazy {
        try {
            cameraManager?.cameraIdList?.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) {
            null
        }
    }

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    @Synchronized
    fun startAlarm(
        soundResId: Int,
        volumePercent: Int,
        flashMode: FlashMode,
        vibrationMode: VibrationMode,
        maxDurationSec: Int
    ) {
        if (_isAlarmRinging.value) return
        _isAlarmRinging.value = true
        Log.d(TAG, "Starting alarm with sound=$soundResId, vol=$volumePercent, flash=$flashMode, vib=$vibrationMode")

        acquireWakeLock(maxDurationSec)
        startAudio(soundResId, volumePercent)
        startFlash(flashMode)
        startVibration(vibrationMode)

        // Auto-stop timeout
        autoStopJob?.cancel()
        autoStopJob = scope.launch {
            delay(maxDurationSec * 1000L)
            stopAlarm()
        }
    }

    @Synchronized
    fun stopAlarm() {
        if (!_isAlarmRinging.value) return
        _isAlarmRinging.value = false
        Log.d(TAG, "Stopping alarm")

        autoStopJob?.cancel()
        autoStopJob = null

        stopAudio()
        stopFlash()
        stopVibration()
        releaseWakeLock()
    }

    private fun startAudio(soundResId: Int, volumePercent: Int) {
        try {
            stopAudio()
            val volume = (volumePercent.coerceIn(10, 100)) / 100f
            mediaPlayer = MediaPlayer.create(context, soundResId)?.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setVolume(volume, volume)
                isLooping = true
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound", e)
        }
    }

    private fun stopAudio() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping media player", e)
        } finally {
            mediaPlayer = null
        }
    }

    private fun startFlash(flashMode: FlashMode) {
        flashJob?.cancel()
        val cId = cameraId ?: return
        if (flashMode == FlashMode.OFF) return

        flashJob = scope.launch {
            try {
                when (flashMode) {
                    FlashMode.CONTINUOUS -> {
                        setTorch(cId, true)
                    }
                    FlashMode.FAST_STROBE -> {
                        while (isActive) {
                            setTorch(cId, true)
                            delay(120)
                            setTorch(cId, false)
                            delay(120)
                        }
                    }
                    FlashMode.SOS -> {
                        val dot = 150L
                        val dash = 350L
                        val gap = 150L
                        val letterGap = 400L

                        while (isActive) {
                            // S: ...
                            repeat(3) { setTorch(cId, true); delay(dot); setTorch(cId, false); delay(gap) }
                            delay(letterGap)
                            // O: ---
                            repeat(3) { setTorch(cId, true); delay(dash); setTorch(cId, false); delay(gap) }
                            delay(letterGap)
                            // S: ...
                            repeat(3) { setTorch(cId, true); delay(dot); setTorch(cId, false); delay(gap) }
                            delay(1000)
                        }
                    }
                    else -> {}
                }
            } catch (e: CancellationException) {
                // Cancelled normally
            } catch (e: Exception) {
                Log.e(TAG, "Flashlight error", e)
            } finally {
                setTorch(cId, false)
            }
        }
    }

    private fun stopFlash() {
        flashJob?.cancel()
        flashJob = null
        val cId = cameraId ?: return
        setTorch(cId, false)
    }

    private fun setTorch(cId: String, enable: Boolean) {
        try {
            cameraManager?.setTorchMode(cId, enable)
        } catch (e: CameraAccessException) {
            // Flash unavailable or in use
        } catch (e: Exception) {
            Log.w(TAG, "Could not set torch mode: ${e.message}")
        }
    }

    private fun startVibration(vibrationMode: VibrationMode) {
        val vib = vibrator ?: return
        if (!vib.hasVibrator()) return
        if (vibrationMode == VibrationMode.OFF) return

        try {
            when (vibrationMode) {
                VibrationMode.STRONG -> {
                    val timings = longArrayOf(0, 500, 200, 500, 200)
                    val amplitudes = intArrayOf(0, 255, 0, 255, 0)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vib.vibrate(VibrationEffect.createWaveform(timings, amplitudes, 0))
                    } else {
                        @Suppress("DEPRECATION")
                        vib.vibrate(timings, 0)
                    }
                }
                VibrationMode.HEARTBEAT -> {
                    val timings = longArrayOf(0, 150, 100, 250, 600)
                    val amplitudes = intArrayOf(0, 200, 0, 255, 0)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vib.vibrate(VibrationEffect.createWaveform(timings, amplitudes, 0))
                    } else {
                        @Suppress("DEPRECATION")
                        vib.vibrate(timings, 0)
                    }
                }
                else -> {}
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error vibrating", e)
        }
    }

    private fun stopVibration() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling vibration", e)
        }
    }

    private fun acquireWakeLock(maxDurationSec: Int) {
        try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = pm?.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "ClapFinder::AlarmWakeLock"
            )?.apply {
                setReferenceCounted(false)
                acquire((maxDurationSec + 5) * 1000L)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Could not acquire wakelock", e)
        }
    }

    private fun releaseWakeLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing wakelock", e)
        } finally {
            wakeLock = null
        }
    }
}

