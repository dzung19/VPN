package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.MainActivity
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.R
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.VpnApp
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.alert.AlarmController
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.ClapPreferencesRepository
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.detection.AudioDetectionEngine
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.AvailableSounds
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ClapDetectionService : Service() {

    companion object {
        private const val TAG = "ClapDetectionService"
        private const val NOTIFICATION_ID = 1001
        private const val ALARM_NOTIFICATION_ID = 1002

        const val ACTION_START = "ACTION_START_DETECTION"
        const val ACTION_STOP = "ACTION_STOP_DETECTION"
        const val ACTION_STOP_ALARM = "ACTION_STOP_ALARM"

        fun startService(context: Context) {
            val intent = Intent(context, ClapDetectionService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, ClapDetectionService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    @Inject lateinit var audioDetectionEngine: AudioDetectionEngine
    @Inject lateinit var alarmController: AlarmController
    @Inject lateinit var preferencesRepository: ClapPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "ClapDetectionService onCreate")

        audioDetectionEngine.onTriggeredListener = { isClap ->
            onDetectionEvent(isClap)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopDetection()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_STOP_ALARM -> {
                alarmController.stopAlarm()
                return START_STICKY
            }
            else -> {
                startForegroundWithNotification()
                startDetection()
                return START_STICKY
            }
        }
    }

    private fun startForegroundWithNotification() {
        val notification = buildOngoingNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startDetection() {
        preferencesRepository.setDetectionActive(true)
        audioDetectionEngine.start()
    }

    private fun stopDetection() {
        preferencesRepository.setDetectionActive(false)
        audioDetectionEngine.stop()
        alarmController.stopAlarm()
    }

    private fun onDetectionEvent(isClap: Boolean) {
        val soundId = preferencesRepository.selectedSoundId.value
        val soundItem = AvailableSounds.getById(soundId)
        val volume = preferencesRepository.volumePercent.value
        val flashMode = preferencesRepository.flashMode.value
        val vibMode = preferencesRepository.vibrationMode.value
        val duration = preferencesRepository.alarmDurationSec.value

        alarmController.startAlarm(
            soundResId = soundItem.soundResId,
            volumePercent = volume,
            flashMode = flashMode,
            vibrationMode = vibMode,
            maxDurationSec = duration
        )

        // Wake screen and bring activity to front
        try {
            val appIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("EXTRA_ALARM_RINGING", true)
            }
            startActivity(appIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Could not launch MainActivity on alarm", e)
        }
    }

    private fun buildOngoingNotification(): Notification {
        val contentIntent = Intent(this, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AlarmActionReceiver::class.java).apply {
            action = "ACTION_STOP_SERVICE"
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, VpnApp.CHANNEL_DETECTION_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_desc))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
            .addAction(0, getString(R.string.action_stop), stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        stopDetection()
        super.onDestroy()
        Log.d(TAG, "ClapDetectionService onDestroy")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

