package com.example.androidvpn.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
// import com.android.billingclient.api.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    // Dummy State Flows for UI development before hooking into Room/SharedPreferences
    private val _remainingDataBytes = MutableStateFlow(0L)
    val remainingDataBytes: StateFlow<Long> = _remainingDataBytes.asStateFlow()

    private val _timePassActiveUntilMs = MutableStateFlow<Long?>(null)
    val timePassActiveUntilMs: StateFlow<Long?> = _timePassActiveUntilMs.asStateFlow()

    private val _availableProducts = MutableStateFlow<List<StoreProduct>>(emptyList())
    val availableProducts: StateFlow<List<StoreProduct>> = _availableProducts.asStateFlow()

    // private lateinit var billingClient: BillingClient
    
    init {
        // Initialize mock products
        _availableProducts.value = listOf(
            StoreProduct("pass_time_12h", "12 Hour Pass", "Unlimited data for 12 hours", "$0.60"),
            StoreProduct("pass_time_24h", "24 Hour Pass", "Unlimited data for 24 hours", "$1.20"),
            StoreProduct("pass_data_1gb", "1 GB Data Pass", "Never expires until consumed", "$1.00"),
            StoreProduct("pass_data_3gb", "3 GB Data Pass", "Never expires until consumed", "$2.50"),
            StoreProduct("pass_data_5gb", "5 GB Data Pass", "Never expires until consumed", "$4.00"),
            StoreProduct("pass_data_10gb", "10 GB Data Pass", "Best value! Never expires", "$7.00")
        )
        
        // TODO: setupBillingClient()
    }

    /*
    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(getApplication())
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                }
            }
            .enablePendingPurchases()
            .build()
            
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    queryProductDetails()
                }
            }
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }
    
    private fun queryProductDetails() {
        // Query Google Play Console for true prices and localized descriptions
    }
    
    private fun handlePurchase(purchase: Purchase) {
        // Verify purchase token with backend (or locally if purely client-side)
        // Consume the product so it can be bought again!
        // e.g. if purchase.products.contains("pass_data_1gb") -> add 1GB to WalletManager
        
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.consumeAsync(consumeParams) { billingResult, outToken ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Grant entitlement to the user
            }
        }
    }
    */

    fun purchaseProduct(productId: String) {
        // Trigger Google Play Billing flow
        viewModelScope.launch {
           // val productDetails = getProductDetails(productId) 
           // val billingFlowParams = BillingFlowParams.newBuilder()
           //     .setProductDetailsParamsList(listOf(
           //         BillingFlowParams.ProductDetailsParams.newBuilder()
           //             .setProductDetails(productDetails)
           //             .build()
           //     ))
           //     .build()
           // billingClient.launchBillingFlow(activity, billingFlowParams)
        }
    }
}
