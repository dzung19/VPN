package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.androidvpn.data.SplitTunnelRepository
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.widget.VpnWidgetProvider
import com.wireguard.android.backend.Tunnel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class VpnActionReceiver : BroadcastReceiver() {

    @Inject lateinit var serverRepository: ServerRepository
    @Inject lateinit var splitTunnelRepository: SplitTunnelRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (action == "DISCONNECT_VPN") {
            CoroutineScope(Dispatchers.IO).launch {
                TunnelManager.stopTunnel()
                updateWidgets(context)
            }
        } else if (action == "TOGGLE_VPN") {
            CoroutineScope(Dispatchers.IO).launch {
                if (TunnelManager.tunnelState.value == Tunnel.State.UP) {
                    TunnelManager.stopTunnel()
                } else {
                    var config = serverRepository.getCurrentConfig()
                    
                    // Specific handling for WARP to refresh endpoints if needed
                    if (config == null || config.name == "Cloudflare WARP") {
                        val freshWarp = serverRepository.createCloudflareConfig()
                        if (freshWarp != null) {
                            config = freshWarp
                        }
                    }

                    if (config != null) {
                        try {
                            TunnelManager.startTunnel(config, splitTunnelRepository.getExcludedApps())
                        } catch (e: Exception) {
                            Log.e("VpnActionReceiver", "Failed to start VPN via widget", e)
                        }
                    }
                }
                updateWidgets(context)
            }
        }
    }

    private fun updateWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val comp = ComponentName(context, VpnWidgetProvider::class.java)
        val ids = appWidgetManager.getAppWidgetIds(comp)
        if (ids.isNotEmpty()) {
            val updateIntent = Intent(context, VpnWidgetProvider::class.java).apply {
                this.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                this.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(updateIntent)
        }
    }
}
