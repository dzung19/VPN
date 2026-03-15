package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.AppInfo
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.SplitTunnelRepository
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.AppIconCache
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.BillingManager
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.WalletManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplitTunnelViewModel @Inject constructor(
    private val repository: SplitTunnelRepository,
    private val billingManager: BillingManager,
    private val walletManager: WalletManager,
    val iconCache: AppIconCache
) : ViewModel() {

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val hasPremiumAccess: StateFlow<Boolean> = combine(
        billingManager.isPremium,
        walletManager.timePassActiveUntilMs,
        walletManager.remainingDataBytes
    ) { isSub, timePass, data ->
        isSub || (timePass != null && timePass > System.currentTimeMillis()) || (data > 0L)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _isLoading.value = true
            _apps.value = repository.getInstalledApps()
            _isLoading.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleApp(packageName: String) {
        // Optimistic UI update for immediate feedback
        _apps.value = _apps.value.map { app ->
            if (app.packageName == packageName) {
                app.copy(isExcluded = !app.isExcluded)
            } else {
                app
            }
        }
        
        // Persist to repository on background thread
        viewModelScope.launch {
            repository.toggleApp(packageName)
            // Optionally, we could reload here if needed, but optimistic update is usually fine
        }
    }
}
