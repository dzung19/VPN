package com.example.androidvpn.ui

import android.app.Application
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidvpn.data.ServerRepository
import com.example.androidvpn.data.TunnelManager
import com.example.androidvpn.model.ServerConfig
import com.wireguard.android.backend.Tunnel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ServerRepository,
    application: Application
) : AndroidViewModel(application) {

    // Repository is injected

    
    // UI State
    val vpnState = TunnelManager.tunnelState
    
    // Connected Duration (Placeholder for now)
    val connectionDuration = MutableStateFlow("00:00:00")
    
    private val _currentConfig = MutableStateFlow<ServerConfig?>(null)
    val currentConfig: StateFlow<ServerConfig?> = _currentConfig

    private val _configs = MutableStateFlow<List<ServerConfig>>(emptyList())
    val configs: StateFlow<List<ServerConfig>> = _configs



    private fun loadData() {
        viewModelScope.launch {
            _configs.value = repository.getConfigs()
            _currentConfig.value = repository.getCurrentConfig()
            
            // Auto-create Cloudflare profile if no configs exist
            if (_configs.value.isEmpty()) {
                createCloudflareConfig()
            }
        }
    }
    
    fun createCloudflareConfig() {
        viewModelScope.launch {
            // TODO: partial loading state
            val config = repository.createCloudflareConfig()
            if (config != null) {
                selectConfig(config)
            }
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
                TunnelManager.startTunnel(config)
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
                kotlinx.coroutines.delay(1000)
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
        return VpnService.prepare(getApplication())
    }
}
