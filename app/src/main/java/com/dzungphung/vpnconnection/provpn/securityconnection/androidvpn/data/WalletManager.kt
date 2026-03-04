package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class WalletManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("wallet_prefs", Context.MODE_PRIVATE)

    private val _remainingDataBytes = MutableStateFlow(prefs.getLong("remaining_data_bytes", 0L))
    val remainingDataBytes: StateFlow<Long> = _remainingDataBytes.asStateFlow()

    private val _timePassActiveUntilMs = MutableStateFlow(
        if (prefs.contains("time_pass_active_until_ms")) prefs.getLong("time_pass_active_until_ms", 0L) else null
    )
    val timePassActiveUntilMs: StateFlow<Long?> = _timePassActiveUntilMs.asStateFlow()

    fun addDataBytes(bytes: Long) {
        val newBalance = _remainingDataBytes.value + bytes
        _remainingDataBytes.value = newBalance
        prefs.edit { putLong("remaining_data_bytes", newBalance) }
    }

    /**
     * Consumes specified bytes from the balance.
     * @return true if there was enough balance and it was consumed, false if balance is 0.
     */
    fun consumeDataBytes(bytes: Long): Boolean {
        val current = _remainingDataBytes.value
        if (current <= 0) return false
        
        val newBalance = maxOf(0L, current - bytes)
        _remainingDataBytes.value = newBalance
        prefs.edit { putLong("remaining_data_bytes", newBalance) }
        return newBalance > 0
    }

    fun addTimePass(hours: Int) {
        val currentTime = System.currentTimeMillis()
        val currentExpiry = _timePassActiveUntilMs.value ?: currentTime
        val effectiveStart = maxOf(currentTime, currentExpiry)
        
        val newExpiry = effectiveStart + (hours * 60L * 60L * 1000L)
        _timePassActiveUntilMs.value = newExpiry
        prefs.edit { putLong("time_pass_active_until_ms", newExpiry) }
    }
}
