package com.example.androidvpn.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingManager"
        const val PREMIUM_PRODUCT_ID = "premium_vpn_monthly"
        // Set to false for production!
        const val TEST_MODE = true
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    private val _billingReady = MutableStateFlow(false)

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    init {
        connectToPlayBilling()
    }

    private fun connectToPlayBilling() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing connected successfully")
                    _billingReady.value = true
                    scope.launch {
                        querySubscriptionProducts()
                        queryExistingPurchases()
                    }
                } else {
                    Log.e(TAG, "Billing setup failed: ${result.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing disconnected, will retry on next action")
                _billingReady.value = false
            }
        })
    }

    private suspend fun querySubscriptionProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        val result = billingClient.queryProductDetails(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val details = result.productDetailsList?.firstOrNull()
            _productDetails.value = details
            if (details != null) {
                Log.d(TAG, "Product found: ${details.title}")
            } else {
                Log.w(TAG, "No product details found for $PREMIUM_PRODUCT_ID")
            }
        } else {
            Log.e(TAG, "Failed to query products: ${result.billingResult.debugMessage}")
        }
    }

    private suspend fun queryExistingPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        val result = billingClient.queryPurchasesAsync(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val activeSub = result.purchasesList.any { purchase ->
                purchase.products.contains(PREMIUM_PRODUCT_ID) &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
            }
            _isPremium.value = activeSub
            Log.d(TAG, "Premium status: $activeSub (${result.purchasesList.size} purchases)")

            // Acknowledge any unacknowledged purchases
            result.purchasesList.forEach { purchase ->
                if (!purchase.isAcknowledged && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    acknowledgePurchase(purchase)
                }
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
        // TEST MODE: bypass Google Play and directly unlock premium
        if (TEST_MODE) {
            Log.d(TAG, "TEST MODE: Unlocking premium without Google Play")
            _isPremium.value = true
            return
        }

        val details = _productDetails.value
        if (details == null) {
            Log.e(TAG, "No product details available")
            return
        }

        // Get the first offer (base plan with free trial)
        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (offerToken == null) {
            Log.e(TAG, "No offer token available")
            return
        }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offerToken)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val result = billingClient.launchBillingFlow(activity, flowParams)
        Log.d(TAG, "Launch billing flow result: ${result.responseCode}")
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        Log.d(TAG, "Purchase successful!")
                        _isPremium.value = true
                        scope.launch { acknowledgePurchase(purchase) }
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "User canceled purchase")
            }
            else -> {
                Log.e(TAG, "Purchase error: ${result.debugMessage}")
            }
        }
    }

    private suspend fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        val result = billingClient.acknowledgePurchase(params)
        Log.d(TAG, "Acknowledge result: ${result.responseCode}")
    }

    fun getFormattedPrice(): String {
        return _productDetails.value
            ?.subscriptionOfferDetails
            ?.firstOrNull()
            ?.pricingPhases
            ?.pricingPhaseList
            ?.lastOrNull() // Last phase = recurring price (not trial)
            ?.formattedPrice
            ?: "$1.99/month"
    }

    fun refresh() {
        if (!_billingReady.value) {
            connectToPlayBilling()
        } else {
            scope.launch {
                queryExistingPurchases()
            }
        }
    }
}
