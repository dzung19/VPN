package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.detection

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

data class DetectionResult(
    val isClap: Boolean,
    val isWhistle: Boolean,
    val clapConfidence: Float,
    val whistleConfidence: Float
)

@Singleton
class YamnetClassifier @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "YamnetClassifier"
        private const val MODEL_FILE = "yamnet.tflite"
        const val EXPECTED_SAMPLE_RATE = 16000
        const val WINDOW_SIZE = 15600 // 0.975s at 16kHz

        // AudioSet class indices for Clapping / Hands
        // 56: Hands, 57: Finger snapping, 58: Clapping, 62: Applause
        private val CLAP_CLASSES = intArrayOf(56, 57, 58, 62)

        // AudioSet class indices for Whistling
        // 35: Whistling, 396: Whistle
        private val WHISTLE_CLASSES = intArrayOf(35, 396)
    }

    private var interpreter: Interpreter? = null
    private var isInitialized = false

    // Pre-allocated buffers to prevent GC thrashing and save battery
    private val inputBuffer: ByteBuffer = ByteBuffer.allocateDirect(WINDOW_SIZE * 4).apply {
        order(ByteOrder.nativeOrder())
    }
    // Output shape: [N, 521]. For 15600 samples, N is typically 1 or 2.
    // We allocate a 2D float array [1][521] or read dynamic shape.
    private var outputArray: Array<FloatArray> = Array(1) { FloatArray(521) }

    init {
        initModel()
    }

    private fun initModel() {
        try {
            val modelBuffer = loadModelFile()
            val options = Interpreter.Options().apply {
                setNumThreads(2) // 2 threads is optimal for low battery consumption and fast inference
                setCancellable(false)
            }
            interpreter = Interpreter(modelBuffer, options)

            // Adjust output array size to output tensor shape
            val outputTensor = interpreter?.getOutputTensor(0)
            if (outputTensor != null) {
                val shape = outputTensor.shape()
                val numScores = if (shape.isNotEmpty()) shape[0] else 1
                val numClasses = if (shape.size > 1) shape[1] else 521
                outputArray = Array(numScores) { FloatArray(numClasses) }
                Log.d(TAG, "YAMNet initialized successfully. Output shape: [${numScores}, ${numClasses}]")
            }
            isInitialized = true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing YAMNet model", e)
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(MODEL_FILE)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Runs inference on the normalized float waveform (size 15600).
     * Synchronized to prevent concurrent inferences on reused buffers.
     */
    @Synchronized
    fun classify(waveform: FloatArray, clapThreshold: Float, whistleThreshold: Float): DetectionResult {
        val tflite = interpreter ?: return DetectionResult(false, false, 0f, 0f)

        try {
            // Write waveform into direct ByteBuffer
            inputBuffer.rewind()
            val limit = minOf(WINDOW_SIZE, waveform.size)
            for (i in 0 until limit) {
                inputBuffer.putFloat(waveform[i])
            }
            // Pad remaining with zeros if needed
            for (i in limit until WINDOW_SIZE) {
                inputBuffer.putFloat(0f)
            }

            // Run inference
            tflite.run(inputBuffer, outputArray)

            var maxClapConfidence = 0.0f
            var maxWhistleConfidence = 0.0f

            for (row in outputArray) {
                for (clapIdx in CLAP_CLASSES) {
                    if (clapIdx < row.size && row[clapIdx] > maxClapConfidence) {
                        maxClapConfidence = row[clapIdx]
                    }
                }
                for (whistleIdx in WHISTLE_CLASSES) {
                    if (whistleIdx < row.size && row[whistleIdx] > maxWhistleConfidence) {
                        maxWhistleConfidence = row[whistleIdx]
                    }
                }
            }

            return DetectionResult(
                isClap = maxClapConfidence >= clapThreshold,
                isWhistle = maxWhistleConfidence >= whistleThreshold,
                clapConfidence = maxClapConfidence,
                whistleConfidence = maxWhistleConfidence
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error during inference", e)
            return DetectionResult(false, false, 0f, 0f)
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
        isInitialized = false
    }
}

