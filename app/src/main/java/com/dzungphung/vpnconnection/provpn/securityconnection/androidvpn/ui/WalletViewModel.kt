package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.BillingManager
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.StoreProduct
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.WalletManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    walletManager: WalletManager,
    private val billingManager: BillingManager
) : ViewModel() {

    val remainingDataBytes: StateFlow<Long> = walletManager.remainingDataBytes
    val timePassActiveUntilMs: StateFlow<Long?> = walletManager.timePassActiveUntilMs

    val availableSubs: StateFlow<ProductDetails?> = billingManager.productDetails

    val availableProducts: StateFlow<List<StoreProduct>> = billingManager.availablePassProducts

    val hasPremiumAccess: StateFlow<Boolean> = combine(
        billingManager.isPremium,
        walletManager.timePassActiveUntilMs,
        walletManager.remainingDataBytes
    ) { isSub, timePass, data ->
        isSub || (timePass != null && timePass > System.currentTimeMillis()) || (data > 0L)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun purchaseProduct(activity: Activity, productId: String) {
        billingManager.launchPassPurchaseFlow(activity, productId)
    }
}
