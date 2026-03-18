package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.BillingManager
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.ServerRepository
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.SplitTunnelRepository
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.TunnelManager
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.WalletManager
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.ServerConfig
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.model.ServerItemDto
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.widget.updateWidget
import com.wireguard.android.backend.Tunnel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: ServerRepository,
    val billingManager: BillingManager,
    walletManager: WalletManager,
    val splitTunnelRepository: SplitTunnelRepository,
    private val tunnelManager: TunnelManager,
) : ViewModel() {

    // Unified Premium State (Subscription OR Active Wallet Passes)
    val hasPremiumAccess: StateFlow<Boolean> = combine(
        billingManager.isPremium,
        walletManager.timePassActiveUntilMs,
        walletManager.remainingDataBytes
    ) { isSub, timePass, data ->
        isSub || (timePass != null && timePass > System.currentTimeMillis()) || (data > 0L)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // UI State
    val vpnState = tunnelManager.tunnelState

    // Connected Duration (Placeholder for now)
    val connectionDuration = MutableStateFlow("00:00:00")

    private val _currentConfig = MutableStateFlow<ServerConfig?>(null)
    val currentConfig: StateFlow<ServerConfig?> = _currentConfig

    private val _configs = MutableStateFlow<List<ServerConfig>>(emptyList())
    val configs: StateFlow<List<ServerConfig>> = _configs

    // New Server List State
    private val _serverList = MutableStateFlow<List<ServerItemDto>>(emptyList())
    val serverList: StateFlow<List<ServerItemDto>> = _serverList

    // Provisioning state (loading indicator for new users)
    private val _isProvisioning = MutableStateFlow(false)
    val isProvisioning: StateFlow<Boolean> = _isProvisioning

    // Latency measurement
    private val _latencyMs = MutableStateFlow<Long?>(-1L)
    val latencyMs: StateFlow<Long?> = _latencyMs


    private fun loadData() {
        viewModelScope.launch {
            _configs.value = repository.getConfigs()
            _currentConfig.value = repository.getCurrentConfig()

            // Fetch server list from API
            if (_serverList.value.isEmpty()) {
                _serverList.value = repository.fetchServers()
            }
        }
    }

    /**
     * Called from ServerListScreen when user picks the WARP (free) server.
     * Only sets a placeholder config — actual registration happens in toggleVpn().
     */
    fun selectWarp() {
        viewModelScope.launch {
            val placeholder = ServerConfig(
                name = "Cloudflare WARP",
                privateKey = "",
                address = "",
                country = "Cloudflare",
                city = "WARP",
                flag = "\uD83C\uDF10",
                isPremium = false
            )
            repository.saveCurrentConfig(placeholder)
            _currentConfig.value = placeholder
        }
    }

    /**
     * Called from ServerListScreen when user picks a premium server.
     * Only stores selection — actual connection happens when user taps CONNECT.
     */
    fun selectServerItem(serverItem: ServerItemDto) {
        viewModelScope.launch {
            val config = ServerConfig(
                name = "${serverItem.flag} ${serverItem.city}",
                privateKey = "",
                address = "",
                country = serverItem.country,
                city = serverItem.city,
                flag = serverItem.flag,
                isPremium = true
            )
            repository.saveCurrentConfig(config)
            _currentConfig.value = config
        }
    }

    suspend fun createCloudflareConfig() {
        Log.d("HomeViewModel", "createCloudflareConfig() called")

        // Stop current tunnel if running
        withContext(NonCancellable) {
            if (vpnState.value == Tunnel.State.UP) {
                tunnelManager.stopTunnel()
            }
        }

        _isProvisioning.value = true

        // Try multiple endpoints until one actually passes traffic
        var connected = false

        for (attempt in 1..MAX_RETRY) {
            val config = repository.createCloudflareConfig()
            if (config == null) {
                Log.e("HomeViewModel", "WARP config creation FAILED (null)")
                continue
            }

            selectConfig(config)

            try {
                tunnelManager.startTunnel(
                    config,
                    splitTunnelRepository.getExcludedApps()
                ) { hasPremiumAccess.value }

                // Wait a moment for tunnel interface to come up
                delay(2000)

                // Verify ACTUAL connectivity (not just tunnel state)
                // WireGuard reports UP before handshake completes!
                val reachable = canReachInternet(config.dns)

                if (reachable) {
                    Log.d("HomeViewModel", "WARP connected on attempt $attempt: ${config.endpoint}")
                    connected = true
                    _currentConfig.value = config
                    break
                }

                // Handshake failed, try next endpoint, and actively delete the stale config
                Log.w(
                    "HomeViewModel",
                    "WARP no connectivity on ${config.endpoint}, deleting and retrying..."
                )
                tunnelManager.stopTunnel()
                repository.removeConfig(config)
                delay(500)

            } catch (e: Exception) {
                Log.e("HomeViewModel", "WARP tunnel error: ${e.message}")
                try {
                    tunnelManager.stopTunnel()
                } catch (_: Throwable) {
                }
                repository.removeConfig(config)
            }
        }

        if (!connected) {
            try {
                tunnelManager.stopTunnel()
            } catch (_: Throwable) {
            }
            _currentConfig.value = null
        }

        _isProvisioning.value = false
    }

    fun selectConfig(config: ServerConfig) {
        viewModelScope.launch {
            repository.saveCurrentConfig(config)
            _currentConfig.value = config
            if (vpnState.value == Tunnel.State.UP) {
                // Restart if running? For now let's just update config reference.
                // Or maybe user has to manually reconnect.
            }
        }
    }

    private var latencyJob: Job? = null

    fun startLatencyMonitor(endpoint: String? = null) {
        latencyJob?.cancel()
        // Use exception handler to prevent ANY crash from latency measurement
        val handler = CoroutineExceptionHandler { _, t ->
            Log.d("Latency", "Coroutine exception: ${t.javaClass.simpleName}")
        }
        latencyJob = viewModelScope.launch(Dispatchers.IO + handler) {
            // Wait for tunnel to stabilize before measuring
            delay(2000L)

            while (true) {
                val ms = try {
                    // Simple TCP connect to Cloudflare DNS ΓÇö fast, reliable, no SSL
                    measureLatencyMs(3000,endpoint)
                } catch (t: Throwable) {
                    Log.d("Latency", "Failed: ${t.javaClass.simpleName}")
                    -1L
                } catch (e: Exception) {
                    -1L
                }
                _latencyMs.value = ms
                delay(40_000L)
            }
        }
    }

    fun stopLatencyMonitor() {
        latencyJob?.cancel()
        latencyJob = null
        _latencyMs.value = null
    }

    // Connect to a specific server item (internal — called from toggleVpn)
    private suspend fun connectToServer(serverItem: ServerItemDto) {
        _isProvisioning.value = true
        val config = repository.connectToServer(serverItem)
        if (config != null) {
            _currentConfig.value = config
            _isProvisioning.value = false
            tunnelManager.startTunnel(
                config,
                splitTunnelRepository.getExcludedApps(),
                { true }
            )
        } else {
            _isProvisioning.value = false
        }
    }

    fun toggleVpn() {
        viewModelScope.launch {
            val config = _currentConfig.value ?: return@launch
            if (vpnState.value == Tunnel.State.UP) {
                tunnelManager.stopTunnel()
            } else {
                // WARP configs go stale after disconnect — always re-register
                if (config.name == "Cloudflare WARP") {
                    Log.d("HomeViewModel", "WARP detected, re-registering fresh keys...")
                    createCloudflareConfig()
                    context.updateWidget()
                    return@launch
                }

                // Premium server — find the matching server item and connect via API
                val serverItem = _serverList.value.find {
                    "${it.flag} ${it.city}" == config.name
                }
                if (serverItem != null) {
                    connectToServer(serverItem)
                } else {
                    Log.e("HomeViewModel", "Could not find server for: ${config.name}")
                }
            }
            context.updateWidget()
        }
    }

    private var timerJob: Job? = null
    private var startTime: Long = 0L

    init {
        loadData()
        startTimerObserver()
        startPeerWatchdog()
    }

    private fun startPeerWatchdog() {
        viewModelScope.launch {
            tunnelManager.isPeerDead.collect { isDead ->
                if (isDead && !_isProvisioning.value && vpnState.value == Tunnel.State.UP) {
                    val config = _currentConfig.value ?: return@collect
                    if (config.name == "Cloudflare WARP") {
                        Log.w(
                            "HomeViewModel",
                            "Watchdog tripped: WARP peer is dead. Auto-reconnecting behind the scenes..."
                        )
                        tunnelManager.isPeerDead.value = false // Reset state
                        _isProvisioning.value = true
                        createCloudflareConfig()
                        context.updateWidget()
                    }
                }
            }
        }
    }

    private fun startTimerObserver() {
        viewModelScope.launch {
            vpnState.collect { state ->
                if (state == Tunnel.State.UP) {
                    startTimer()
                } else {
                    stopTimer()
                }
            }
        }
    }

    private fun startTimer() {
        if (timerJob?.isActive == true) return
        startTime = System.currentTimeMillis()
        timerJob = viewModelScope.launch {
            while (true) {
                val elapsed = System.currentTimeMillis() - startTime
                connectionDuration.value = formatDuration(elapsed)
                delay(1000)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        connectionDuration.value = "00:00:00"
    }

    private fun formatDuration(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60))
        return String.format(Locale.getDefault(),"%02d:%02d:%02d", hours, minutes, seconds)
    }

    private suspend fun canReachInternet(
        config: String?,
        timeoutMs: Int = 2_500
    ): Boolean = withContext(Dispatchers.IO) {
        val targets = buildProbeTargets(config)

        targets.any { target ->
            runCatching {
                Socket().use { socket ->
                    socket.connect(target, timeoutMs)
                }
                true
            }.getOrElse { error ->
                Log.d("HomeViewModel", "Probe failed for ${target.hostString}:${target.port}: ${error.message}")
                false
            }
        }
    }

    private suspend fun measureLatencyMs(timeoutMs: Int = 2_500, dns: String?): Long = withContext(Dispatchers.IO) {
        val targets = buildProbeTargets(dns)

        for (target in targets) {
            val result = runCatching {
                val start = System.currentTimeMillis()
                Socket().use { socket ->
                    socket.connect(target, timeoutMs)
                }
                System.currentTimeMillis() - start
            }

            result.onSuccess { return@withContext it }
            result.onFailure { error ->
                Log.d("Latency", "Failed for ${target.hostString}:${target.port}: ${error.javaClass.simpleName}")
            }
        }

        -1L
    }

    private fun buildProbeTargets(config: String?): List<InetSocketAddress> {
        val hosts = buildList {
            config
                ?.split(",")
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?.let(::addAll)

            add("1.1.1.1")
            add("1.0.0.1")
        }.distinct()

        return hosts.flatMap { host ->
            listOf(
                InetSocketAddress(host, 53),
                InetSocketAddress(host, 443)
            )
        }
    }

    fun checkVpnPermission(): Intent? {
        return VpnService.prepare(context)
    }

    companion object {
        private const val MAX_RETRY = 10
    }
}
