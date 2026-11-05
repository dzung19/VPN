package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.detection

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.PowerManager
import android.os.Process
import android.util.Log
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.ClapPreferencesRepository
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.DetectionMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class AudioDetectionEngine @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val yamnetClassifier: YamnetClassifier,
    private val preferencesRepository: ClapPreferencesRepository
) {
    companion object {
        private const val TAG = "AudioDetectionEngine"
        private const val SAMPLE_RATE = 16000
        private const val CHUNK_SIZE = 4000 // 250ms chunks
        private const val WINDOW_SIZE = 15600 // 0.975s window for YAMNet

        // Base peak thresholds (out of 32767) for Energy Gate
        private const val BASE_ENERGY_GATE_PEAK = 4500
        private const val BASE_CLAP_SCORE_THRESHOLD = 0.28f
        private const val BASE_WHISTLE_SCORE_THRESHOLD = 0.32f
    }

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    // Pre-allocated circular ring buffer and normalized float buffer (Zero-GC)
    private val ringBuffer = ShortArray(WINDOW_SIZE)
    private var ringHead = 0
    private val floatWindow = FloatArray(WINDOW_SIZE)
    private val chunkBuffer = ShortArray(CHUNK_SIZE)

    // False alarm filter: tracks last clap timestamp
    private var lastClapDetectedTime = 0L
    private var consecutiveClapCount = 0

    @Volatile
    private var isRunning = false

    var onTriggeredListener: ((isClap: Boolean) -> Unit)? = null

    @SuppressLint("MissingPermission")
    @Synchronized
    fun start() {
        if (isRunning) return
        isRunning = true

        val minBufSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = maxOf(minBufSize, CHUNK_SIZE * 2)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord failed to initialize")
                stop()
                return
            }

            audioRecord?.startRecording()
            Log.d(TAG, "AudioDetectionEngine started successfully")

            recordingJob = scope.launch {
                // Set low background thread priority to conserve CPU and battery
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
                runDetectionLoop()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting AudioRecord", e)
            stop()
        }
    }

    private suspend fun runDetectionLoop() {
        while (isRunning && currentCoroutineContext().isActive) {
            val record = audioRecord ?: break

            // Blocking read: Thread sleeps at the OS level until CHUNK_SIZE samples are ready (250ms)
            val readSamples = record.read(chunkBuffer, 0, CHUNK_SIZE)
            if (readSamples <= 0) {
                delay(50)
                continue
            }

            // Write chunk to circular ring buffer
            for (i in 0 until readSamples) {
                ringBuffer[ringHead] = chunkBuffer[i]
                ringHead = (ringHead + 1) % WINDOW_SIZE
            }

            // Check if user enabled "Only when screen is off"
            val onlyWhenScreenOff = preferencesRepository.onlyWhenScreenOff.value
            val isScreenOn = powerManager?.isInteractive == true
            if (onlyWhenScreenOff && isScreenOn) {
                // Phone is being actively used by user; skip running AI inference to save battery
                continue
            }

            // ---------------------------------------------------------
            // TẦNG 1: ACOUSTIC ENERGY GATE (~0.1% CPU)
            // ---------------------------------------------------------
            var peak = 0
            var sumSquares = 0L
            for (i in 0 until readSamples) {
                val sample = chunkBuffer[i].toInt()
                val absVal = if (sample < 0) -sample else sample
                if (absVal > peak) peak = absVal
                sumSquares += (sample.toLong() * sample.toLong())
            }
            val rms = sqrt((sumSquares / readSamples).toDouble()).toInt()

            val sensitivity = preferencesRepository.sensitivity.value
            val gateThreshold = (BASE_ENERGY_GATE_PEAK * sensitivity.thresholdScale).toInt()

            // If ambient sound is quiet, discard chunk and wait for next read without invoking AI
            if (peak < gateThreshold || rms < (gateThreshold / 4)) {
                continue
            }

            // ---------------------------------------------------------
            // TẦNG 2: ON-DEMAND AI INFERENCE (YAMNet)
            // ---------------------------------------------------------
            // Extract the latest 15,600 samples (~0.975s) from ring buffer in correct chronological order
            var tail = ringHead
            for (i in 0 until WINDOW_SIZE) {
                val s = ringBuffer[tail]
                floatWindow[i] = s / 32768.0f // Normalize to [-1.0, 1.0]
                tail = (tail + 1) % WINDOW_SIZE
            }

            val mode = preferencesRepository.detectionMode.value
            val clapThresh = BASE_CLAP_SCORE_THRESHOLD * sensitivity.thresholdScale
            val whistleThresh = BASE_WHISTLE_SCORE_THRESHOLD * sensitivity.thresholdScale

            val result = yamnetClassifier.classify(floatWindow, clapThresh, whistleThresh)

            val now = System.currentTimeMillis()

            // Check Whistle
            val isWhistleEligible = (mode == DetectionMode.WHISTLE_ONLY || mode == DetectionMode.CLAP_AND_WHISTLE)
            if (isWhistleEligible && result.isWhistle) {
                Log.d(TAG, "Whistle detected with confidence: ${result.whistleConfidence}")
                triggerAlert(isClap = false)
                continue
            }

            // Check Clap
            val isClapEligible = (mode == DetectionMode.CLAP_ONLY || mode == DetectionMode.CLAP_AND_WHISTLE)
            if (isClapEligible && result.isClap) {
                Log.d(TAG, "Clap candidate detected with confidence: ${result.clapConfidence}")
                // Confirm with double-clap within 1.5 seconds or very high confidence (> 0.50)
                if (result.clapConfidence >= (clapThresh * 1.5f)) {
                    triggerAlert(isClap = true)
                    consecutiveClapCount = 0
                } else {
                    if (now - lastClapDetectedTime < 1500 && now - lastClapDetectedTime > 200) {
                        consecutiveClapCount++
                        if (consecutiveClapCount >= 1) {
                            Log.d(TAG, "Double-clap confirmed!")
                            triggerAlert(isClap = true)
                            consecutiveClapCount = 0
                        }
                    } else {
                        consecutiveClapCount = 1
                    }
                    lastClapDetectedTime = now
                }
            }
        }
    }

    private fun triggerAlert(isClap: Boolean) {
        scope.launch(Dispatchers.Main) {
            onTriggeredListener?.invoke(isClap)
        }
    }

    @Synchronized
    fun stop() {
        isRunning = false
        recordingJob?.cancel()
        recordingJob = null

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioRecord", e)
        } finally {
            audioRecord = null
        }
        Log.d(TAG, "AudioDetectionEngine stopped")
    }
}

