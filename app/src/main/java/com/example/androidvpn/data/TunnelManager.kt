package com.example.androidvpn.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.androidvpn.MainActivity
import com.example.androidvpn.R
import com.example.androidvpn.model.ServerConfig
import com.wireguard.android.backend.Backend
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import com.wireguard.config.InetNetwork
import com.wireguard.config.Interface
import com.wireguard.config.Peer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress

object TunnelManager {
    private const val TAG = "TunnelManager"
    private const val CHANNEL_ID = "vpn_channel"
    private const val NOTIFICATION_ID = 1
    private var backend: Backend? = null
    private var currentTunnel: InternalTunnel? = null
    private var appContext: Context? = null

    private val _tunnelState = MutableStateFlow(Tunnel.State.DOWN)
    val tunnelState: StateFlow<Tunnel.State> = _tunnelState.asStateFlow()

    // Initialize the GoBackend (must be called with Context)
    fun init(context: Context) {
        appContext = context.applicationContext
        if (backend == null) {
            backend = GoBackend(appContext!!)
        }
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "VPN Status",
            NotificationManager.IMPORTANCE_LOW // Low = no sound, shows in shade
        ).apply {
            description = "Shows when VPN is connected"
            setShowBadge(false)
        }
        val manager =
            appContext?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.createNotificationChannel(channel)
    }

    private var speedMonitorJob: kotlinx.coroutines.Job? = null
    private var lastRxBytes: Long = 0
    private var lastTxBytes: Long = 0

    private fun showVpnNotification(
        serverName: String,
        rxSpeed: String = "0 B/s",
        txSpeed: String = "0 B/s"
    ) {
        val ctx = appContext ?: return
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            ctx, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val disconnectIntent = Intent(ctx, VpnActionReceiver::class.java).apply {
            action = "DISCONNECT_VPN"
        }
        val disconnectPendingIntent = PendingIntent.getBroadcast(
            ctx, 1, disconnectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("VPN Connected: $serverName")
            .setContentText("\u2193 $rxSpeed  \u2191 $txSpeed")
            .setOngoing(true) // Can't be swiped away
            .setOnlyAlertOnce(true) // Don't sound/vibrate on every speed update
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Disconnect", disconnectPendingIntent)
            .build()

        val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, notification)
    }

    private fun hideVpnNotification() {
        val manager =
            appContext?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.cancel(NOTIFICATION_ID)
    }

    private fun startSpeedMonitor(serverName: String) {
        speedMonitorJob?.cancel()
        lastRxBytes = 0
        lastTxBytes = 0

        speedMonitorJob = kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            while (tunnelState.value == Tunnel.State.UP) {
                try {
                    val stats = backend?.getStatistics(currentTunnel!!)
                    if (stats != null) {
                        val totalRx = stats.totalRx()
                        val totalTx = stats.totalTx()

                        if (lastRxBytes > 0 || lastTxBytes > 0) {
                            val rxDelta = totalRx - lastRxBytes
                            val txDelta = totalTx - lastTxBytes

                            val rxSpeed = formatBytes(rxDelta) + "/s"
                            val txSpeed = formatBytes(txDelta) + "/s"

                            showVpnNotification(serverName, rxSpeed, txSpeed)
                        }

                        lastRxBytes = totalRx
                        lastTxBytes = totalTx
                    }
                } catch (e: Exception) {
                    // Ignore transient errors reading stats
                }
                delay(1000L)
            }
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format("%.1f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format("%.1f MB", mb)
        return String.format("%.1f GB", mb / 1024.0)
    }

    suspend fun startTunnel(config: ServerConfig, excludedApps: Set<String> = emptySet()) =
        withContext(Dispatchers.IO) {
            try {
                val wgConfig = buildWireGuardConfig(config, excludedApps)

                // Log the config for debugging (Redact private key)
                Log.d(
                    TAG,
                    "Starting Tunnel with config: Interface=${wgConfig.`interface`}, Peers=${wgConfig.peers}"
                )
                Log.d(TAG, "Excluded apps: ${excludedApps.size}")

                val newTunnel = InternalTunnel("wg0")
                currentTunnel = newTunnel

                // Use setState instead of apply (which conflicts with Kotlin's scope function)
                backend?.setState(newTunnel, Tunnel.State.UP, wgConfig)
                _tunnelState.value = Tunnel.State.UP
                Log.d(TAG, "Tunnel Started: ${config.name}")

                // Show persistent notification
                showVpnNotification(config.name)
                startSpeedMonitor(config.name)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start tunnel: ${e.message}", e)
                _tunnelState.value = Tunnel.State.DOWN
                speedMonitorJob?.cancel()
                hideVpnNotification()
                if (e is com.wireguard.android.backend.BackendException) {
                    Log.e(TAG, "Backend Reason: ${e.reason}", e)
                }
            }
        }

    suspend fun stopTunnel() = withContext(Dispatchers.IO) {
        try {
            currentTunnel?.let { tunnel ->
                backend?.setState(tunnel, Tunnel.State.DOWN, null)
            }
            _tunnelState.value = Tunnel.State.DOWN
            Log.d(TAG, "Tunnel Stopped")
            currentTunnel = null
            speedMonitorJob?.cancel()
            hideVpnNotification()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop tunnel: ${e.message}", e)
            _tunnelState.value = Tunnel.State.DOWN
            speedMonitorJob?.cancel()
            hideVpnNotification()
        }
    }

    private fun buildWireGuardConfig(
        serverConfig: ServerConfig,
        excludedApps: Set<String> = emptySet()
    ): Config {
        val interfaceBuilder = Interface.Builder()

        // Parse addresses
        serverConfig.address.split(",").map { it.trim() }.forEach {
            if (it.isNotEmpty()) {
                try {
                    interfaceBuilder.addAddress(InetNetwork.parse(it))
                } catch (e: Exception) {
                    Log.e(TAG, "Invalid Address: $it", e)
                }
            }
        }

        // Parse DNS
        serverConfig.dns.split(",").map { it.trim() }.forEach {
            if (it.isNotEmpty()) {
                try {
                    interfaceBuilder.addDnsServer(InetAddress.getByName(it))
                } catch (e: Exception) {
                    Log.e(TAG, "Invalid DNS: $it", e)
                }
            }
        }

        if (serverConfig.privateKey.isNotEmpty()) {
            interfaceBuilder.parsePrivateKey(serverConfig.privateKey)
        } else {
            throw IllegalArgumentException("Private Key Not Found")
        }

        // Apply split tunneling - exclude selected apps
        excludedApps.forEach { packageName ->
            try {
                interfaceBuilder.excludeApplication(packageName)
            } catch (e: Exception) {
                Log.w(TAG, "Could not exclude app: $packageName", e)
            }
        }

        // Set MTU if specified (1280 recommended for WARP)
        if (serverConfig.mtu > 0) {
            interfaceBuilder.setMtu(serverConfig.mtu)
        }

        val peerBuilder = Peer.Builder()
        peerBuilder.parsePublicKey(serverConfig.publicKey)
        if (serverConfig.endpoint.isNotEmpty()) {
            peerBuilder.parseEndpoint(serverConfig.endpoint)
        }

        serverConfig.allowedIps.split(",").map { it.trim() }.forEach {
            if (it.isNotEmpty()) {
                try {
                    peerBuilder.addAllowedLib(InetNetwork.parse(it))
                } catch (e: Exception) {
                    Log.e(TAG, "Invalid AllowedIP: $it", e)
                }
            }
        }

        // Add dummy PersistentKeepalive to avoid timeouts through NAT
        peerBuilder.setPersistentKeepalive(25)

        val config = Config.Builder()
            .setInterface(interfaceBuilder.build())
            .addPeer(peerBuilder.build())
            .build()

        Log.d(
            TAG, "Final WG Config Content:\n" +
                    "Interface: [Address: ${config.`interface`.addresses}, DNS: ${config.`interface`.dnsServers}, PrivateKey: REDACTED]\n" +
                    "Peer: [PublicKey: ${config.peers[0].publicKey.toBase64()}, Endpoint: ${
                        config.peers[0].endpoint.orElse(
                            null
                        )
                    }, AllowedIPs: ${config.peers[0].allowedIps}]"
        )

        return config
    }

    // Extension function helper
    private fun Peer.Builder.addAllowedLib(network: InetNetwork) = this.addAllowedIp(network)

    // Internal class implementing WireGuard's Tunnel interface
    class InternalTunnel(private val name: String) : Tunnel {
        override fun getName() = name
        override fun onStateChange(newState: Tunnel.State) {
            _tunnelState.value = newState
        }
    }
}
