package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.daumo.ads.DynamicAdsManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VpnApp : Application() {

    companion object {
        const val CHANNEL_DETECTION_ID = "clap_detection_service_channel"
        const val CHANNEL_ALARM_ID = "clap_alarm_alert_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()

        if (isMainProcess()) {
            try {
                val adsDisabled = try {
                    BuildConfig.ADS_DISABLED
                } catch (e: Exception) {
                    Log.e("VpnApp", "Error reading BuildConfig.ADS_DISABLED", e)
                    false
                }
                DynamicAdsManager.initialize(this, adsDisabled)
            } catch (e: Exception) {
                Log.e("DynamicAdsManager", "init error", e)
            }
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Service Foreground Channel (low priority, silent)
            val serviceChannel = NotificationChannel(
                CHANNEL_DETECTION_ID,
                "Clap Detection Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows persistent notification while listening for clap/whistle"
                setShowBadge(false)
            }

            // High Priority Alarm Channel (urgent heads-up)
            val alarmChannel = NotificationChannel(
                CHANNEL_ALARM_ID,
                "Clap Alarm Alert",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Triggered when clap or whistle is detected"
                enableVibration(true)
            }

            notificationManager?.createNotificationChannel(serviceChannel)
            notificationManager?.createNotificationChannel(alarmChannel)
        }
    }

    private fun isMainProcess(): Boolean {
        return applicationInfo.packageName == getProcessName()
    }
}
