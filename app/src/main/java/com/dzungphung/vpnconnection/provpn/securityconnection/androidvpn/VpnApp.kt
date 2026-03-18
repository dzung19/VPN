package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn

import android.app.Application
import android.util.Log
import com.daumo.ads.DynamicAdsManager
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.TunnelManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class VpnApp : Application() {
    @Inject lateinit var tunnelManager: TunnelManager

    override fun onCreate() {
        super.onCreate()
        // Initialize WireGuard Backend
        tunnelManager.init()
        if (isMainProcess()) {
            try {
                // Check if ads should be disabled
                val adsDisabled = try {
                    BuildConfig.ADS_DISABLED
                } catch (e: Exception) {
                    Log.e("RingtoneApplication", "Error reading BuildConfig.ADS_DISABLED", e)
                    false // Default to false if we can't determine the value
                }

                DynamicAdsManager.initialize(this, adsDisabled)
            } catch (e: Exception) {
                Log.e("DynamicAdsManager", "init error", e)
            }
        }
    }

    private fun isMainProcess(): Boolean {
        return applicationInfo.packageName == getProcessName()
    }
}
