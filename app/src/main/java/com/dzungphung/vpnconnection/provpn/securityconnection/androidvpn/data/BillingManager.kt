package com.dzungphung.vpnconnection.provpn.securityconnection.androidvpn.data

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

data class StoreProduct(
    val id: String,
    val name: String,
    val description: String,
    val price: String
)

@Singleton
class BillingManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val walletManager: WalletManager
) : PurchasesUpdatedListener {

    companion object {
        private const val TAG = "BillingManager"
        const val PREMIUM_PRODUCT_ID = "premium_vpn_monthly"
        val PASS_PRODUCT_IDS = listOf(
            "pass-time-12h", "pass-time-24h",
            "pass-data-1gb", "pass-data-3gb", "pass-data-5gb", "pass-data-10gb"
        )
        // Set to false for production!
        const val TEST_MODE = false
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    private val _availablePassProducts = MutableStateFlow<List<StoreProduct>>(emptyList())
    val availablePassProducts: StateFlow<List<StoreProduct>> = _availablePassProducts.asStateFlow()
    private var passProductDetailsList: List<ProductDetails> = emptyList()

    private val _billingReady = MutableStateFlow(false)

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
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
                        queryPassProducts()
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

    private suspend fun queryPassProducts() {
        val productList = PASS_PRODUCT_IDS.map { 
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        val result = billingClient.queryProductDetails(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            this.passProductDetailsList = result.productDetailsList ?: emptyList()
            val uiProducts = this.passProductDetailsList.map { details ->
                StoreProduct(
                    id = details.productId,
                    name = details.name,
                    description = details.description,
                    price = details.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
                )
            }
            _availablePassProducts.value = uiProducts.sortedBy { it.id }
        }
    }

    private suspend fun queryExistingPurchases() {
        // Query subscriptions
        val subsParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        val subsResult = billingClient.queryPurchasesAsync(subsParams)
        
        if (subsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            val activeSub = subsResult.purchasesList.any { purchase ->
                purchase.products.contains(PREMIUM_PRODUCT_ID) &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
            }
            _isPremium.value = activeSub
            Log.d(TAG, "Premium status: $activeSub (${subsResult.purchasesList.size} purchases)")

            subsResult.purchasesList.forEach { purchase ->
                if (!purchase.isAcknowledged && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    acknowledgeSubscription(purchase)
                }
            }
        }

        // Query INAPP purchases to consume any pending passes that got stuck
        val inappParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        val inappResult = billingClient.queryPurchasesAsync(inappParams)
        if (inappResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            inappResult.purchasesList.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    consumePassPurchase(purchase)
                }
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity) {
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

    fun launchPassPurchaseFlow(activity: Activity, productId: String) {
        val details = passProductDetailsList.find { it.productId == productId }
        if (details == null) {
            Log.e(TAG, "No product details available for pass: $productId")
            return
        }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        Log.d(TAG, "Purchase successful!")
                        if (purchase.products.contains(PREMIUM_PRODUCT_ID)) {
                            _isPremium.value = true
                            scope.launch { acknowledgeSubscription(purchase) }
                        } else {
                            // It's a pass purchase
                            scope.launch { consumePassPurchase(purchase) }
                        }
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

    private suspend fun acknowledgeSubscription(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        val result = billingClient.acknowledgePurchase(params)
        Log.d(TAG, "Acknowledge result: ${result.responseCode}")
    }

    private fun consumePassPurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.consumeAsync(consumeParams) { billingResult, _ ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (productId in purchase.products) {
                    when (productId) {
                        "pass-time-12h" -> walletManager.addTimePass(12)
                        "pass-time-24h" -> walletManager.addTimePass(24)
                        "pass-data-1gb" -> walletManager.addDataBytes(1L * 1024 * 1024 * 1024)
                        "pass-data-3gb" -> walletManager.addDataBytes(3L * 1024 * 1024 * 1024)
                        "pass-data-5gb" -> walletManager.addDataBytes(5L * 1024 * 1024 * 1024)
                        "pass-data-10gb" -> walletManager.addDataBytes(10L * 1024 * 1024 * 1024)
                    }
                }
            }
        }
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
