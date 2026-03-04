package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.BillingManager
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.StoreProduct
import com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data.WalletManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    walletManager: WalletManager,
    private val billingManager: BillingManager
) : ViewModel() {

    val remainingDataBytes: StateFlow<Long> = walletManager.remainingDataBytes
    val timePassActiveUntilMs: StateFlow<Long?> = walletManager.timePassActiveUntilMs

    val availableProducts: StateFlow<List<StoreProduct>> = billingManager.availablePassProducts

    fun purchaseProduct(activity: Activity, productId: String) {
        billingManager.launchPassPurchaseFlow(activity, productId)
    }
}
