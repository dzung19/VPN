package com.example.androidvpn.ui

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidvpn.data.BillingManager
import com.example.androidvpn.data.ServerRepository
import com.example.androidvpn.data.SplitTunnelRepository
import com.example.androidvpn.data.TunnelManager
import com.example.androidvpn.model.ServerConfig
import com.example.androidvpn.model.ServerItemDto
import com.wireguard.android.backend.Tunnel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val repository: ServerRepository,
    val billingManager: BillingManager,
    val splitTunnelRepository: SplitTunnelRepository,
) : ViewModel() {

    // Billing state
    val isPremium = billingManager.isPremium

    
    // UI State
    val vpnState = TunnelManager.tunnelState
    
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
            if (_serverList.value.isEmpty())
                _serverList.value = repository.fetchServers()

            // Free tier: auto-create WARP config if no config exists
            if (_currentConfig.value == null) {
                createCloudflareConfig()
            }
        }
    }
    
    fun createCloudflareConfig() {
        viewModelScope.launch {
            Log.d("HomeViewModel", "createCloudflareConfig() called")

            // Stop current tunnel if running
            withContext(NonCancellable) {
                if (vpnState.value == Tunnel.State.UP) {
                    TunnelManager.stopTunnel()
                }
            }

            _isProvisioning.value = true

            // Try multiple endpoints until one actually passes traffic
            val maxRetries = repository.cloudflareService.getEndpointCount()
            var connected = false

            for (attempt in 1..maxRetries) {
                val config = repository.createCloudflareConfig()
                if (config == null) {
                    Log.e("HomeViewModel", "WARP config creation FAILED (null)")
                    continue
                }

                Log.d("HomeViewModel", "WARP attempt $attempt/$maxRetries: ${config.endpoint}")
                selectConfig(config)

                try {
                    TunnelManager.startTunnel(config, splitTunnelRepository.getExcludedApps())

                    // Wait a moment for tunnel interface to come up
                    delay(2000)

                    // Verify ACTUAL connectivity (not just tunnel state)
                    // WireGuard reports UP before handshake completes!
                    val reachable = withContext(Dispatchers.IO) {
                        try {
                            val socket = java.net.Socket()
                            socket.connect(InetSocketAddress("1.1.1.1", 53), 6000)
                            socket.close()
                            true
                        } catch (t: Throwable) {
                            Log.d("HomeViewModel", "Connectivity check failed: ${t.javaClass.simpleName}")
                            false
                        }
                    }

                    if (reachable) {
                        Log.d("HomeViewModel", "WARP connected on attempt $attempt: ${config.endpoint}")
                        connected = true
                        break
                    }

                    // Handshake failed, try next endpoint
                    Log.w("HomeViewModel", "WARP no connectivity on ${config.endpoint}, trying next...")
                    TunnelManager.stopTunnel()
                    delay(500)

                } catch (e: Exception) {
                    Log.e("HomeViewModel", "WARP tunnel error: ${e.message}")
                    try { TunnelManager.stopTunnel() } catch (_: Throwable) {}
                }
            }

            if (!connected) {
                Log.e("HomeViewModel", "WARP: All endpoints failed after $maxRetries attempts")
                try { TunnelManager.stopTunnel() } catch (_: Throwable) {}
            }

            _isProvisioning.value = false
        }
    }
    
    fun addConfig(config: ServerConfig) {
        viewModelScope.launch {
            repository.addConfig(config)
            _configs.value = repository.getConfigs()
        }
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

    private var latencyJob: kotlinx.coroutines.Job? = null

    fun startLatencyMonitor(endpoint: String? = null) {
        latencyJob?.cancel()
        // Use exception handler to prevent ANY crash from latency measurement
        val handler = kotlinx.coroutines.CoroutineExceptionHandler { _, t ->
            Log.d("Latency", "Coroutine exception: ${t.javaClass.simpleName}")
        }
        latencyJob = viewModelScope.launch(Dispatchers.IO + handler) {
            // Wait for tunnel to stabilize before measuring
            delay(5000L)

            while (true) {
                val ms = try {
                    // Simple TCP connect to Cloudflare DNS ΓÇö fast, reliable, no SSL
                    val socket = Socket()
                    val start = System.currentTimeMillis()
                    socket.connect(InetSocketAddress("1.1.1.1", 53), 3000)
                    val elapsed = System.currentTimeMillis() - start
                    socket.close()
                    elapsed
                } catch (t: Throwable) {
                    Log.d("Latency", "Failed: ${t.javaClass.simpleName}")
                    -1L
                }
                withContext(Dispatchers.Main) {
                    _latencyMs.value = ms
                }
                delay(40_000L)
            }
        }
    }

    fun stopLatencyMonitor() {
        latencyJob?.cancel()
        latencyJob = null
        _latencyMs.value = null
    }

    // Connect to a specific server item (from UI)
    fun connectToServer(serverItem: ServerItemDto) {
        viewModelScope.launch {
            // Stop current tunnel if running
            withContext(NonCancellable) {
                if (vpnState.value == Tunnel.State.UP) {
                    TunnelManager.stopTunnel()
                }

                _isProvisioning.value = true
                val config = repository.connectToServer(serverItem)
                if (config != null) {
                    _currentConfig.value = config
                    // VM adds peer instantly via HTTP API, no delay needed
                    _isProvisioning.value = false
                    TunnelManager.startTunnel(config, splitTunnelRepository.getExcludedApps())
                } else {
                    _isProvisioning.value = false
                }
            }
        }
    }

    // Parse raw wireguard config text (Basic helper)
    fun parseAndAddConfig(name: String, configText: String) {
        // This is a naive parser. Ideally use com.wireguard.config.Config.parse
        // implementing basic parsing for now or try-catch block if library available.
        try {
            // Placeholder: Assume library usage or manual parsing
            // For this step, we'll create a dummy config with the text if simple
            // Real implementation needs robust parsing.
            val lines = configText.lines()
            var privateKey = ""
            var address = ""
            var dns = ""
            var publicKey = ""
            var endpoint = ""
            var allowedIps = "0.0.0.0/0"
            
            // Very simple parser
            lines.forEach { line ->
                when {
                    line.startsWith("PrivateKey") -> privateKey = line.substringAfter("=").trim()
                    line.startsWith("Address") -> address = line.substringAfter("=").trim()
                    line.startsWith("DNS") -> dns = line.substringAfter("=").trim()
                    line.startsWith("PublicKey") -> publicKey = line.substringAfter("=").trim()
                    line.startsWith("Endpoint") -> endpoint = line.substringAfter("=").trim()
                    line.startsWith("AllowedIPs") -> allowedIps = line.substringAfter("=").trim()
                }
            }
            
            if (privateKey.isNotEmpty() && endpoint.isNotEmpty()) {
                val newConfig = ServerConfig(name, privateKey, address, dns, publicKey, endpoint, allowedIps)
                addConfig(newConfig)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun toggleVpn() {
        viewModelScope.launch {
            val config = _currentConfig.value ?: return@launch
            if (vpnState.value == Tunnel.State.UP) {
                TunnelManager.stopTunnel()
            } else {
                // WARP configs go stale after disconnect ΓÇö always re-register
                if (config.name == "Cloudflare WARP") {
                    Log.d("HomeViewModel", "WARP detected, re-registering fresh keys...")
                    createCloudflareConfig()
                    return@launch
                }

                val excluded = splitTunnelRepository.getExcludedApps()
                Log.d("HomeViewModel", "toggleVpn: excluded apps = ${excluded.size}")
                TunnelManager.startTunnel(config, excluded)
            }
        }
    }
    
    private var timerJob: kotlinx.coroutines.Job? = null
    private var startTime: Long = 0L

    init {
        loadData()
        startTimerObserver()
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
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun checkVpnPermission(): Intent? {
        return VpnService.prepare(context)
    }
}
