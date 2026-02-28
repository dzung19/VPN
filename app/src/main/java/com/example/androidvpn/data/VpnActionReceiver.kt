package com.example.androidvpn.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.androidvpn.MainActivity
import com.example.androidvpn.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VpnActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "DISCONNECT_VPN") {
            Log.d("VpnActionReceiver", "Disconnect requested from notification")
            CoroutineScope(Dispatchers.IO).launch {
                TunnelManager.stopTunnel()
            }
        }
    }
}
