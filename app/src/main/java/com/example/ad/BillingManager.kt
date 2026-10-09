package com.example.ad

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.data.GamePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * BillingManager handles Google Play Billing integration:
 * - Connection management with automated retry
 * - Querying product details for the non-consumable 'remove_ads' in-app item
 * - Launching the purchase flow via Google Play's native bottom sheet
 * - Mandatory purchase acknowledgment to prevent Google Play's 3-day auto-refund
 * - Restoring existing purchases across device reinstallations
 */
class BillingManager(
    private val context: Context,
    private val preferences: GamePreferences
) : PurchasesUpdatedListener {

    private val _isAdsRemoved = MutableStateFlow(preferences.adsRemoved.value)
    val isAdsRemoved: StateFlow<Boolean> = _isAdsRemoved.asStateFlow()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    private val _formattedPrice = MutableStateFlow<String?>(null)
    val formattedPrice: StateFlow<String?> = _formattedPrice.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private var isConnecting = false

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    /**
     * Connects to Google Play Billing service.
     */
    fun startConnection(onReady: (() -> Unit)? = null) {
        if (billingClient.isReady) {
            queryProductDetails()
            queryExistingPurchases()
            onReady?.invoke()
            return
        }
        if (isConnecting) return

        isConnecting = true
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                isConnecting = false
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails()
                    queryExistingPurchases()
                    onReady?.invoke()
                }
            }

            override fun onBillingServiceDisconnected() {
                isConnecting = false
            }
        })
    }

    /**
     * Queries details and localized price for 'remove_ads'.
     */
    fun queryProductDetails(onComplete: ((ProductDetails?) -> Unit)? = null) {
        if (!billingClient.isReady) {
            startConnection { queryProductDetails(onComplete) }
            return
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(AdConfig.PRODUCT_REMOVE_ADS) // 'remove_ads'
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                productDetailsList.isNotEmpty()
            ) {
                val details = productDetailsList.firstOrNull {
                    it.productId == AdConfig.PRODUCT_REMOVE_ADS
                }
                _productDetails.value = details
                _formattedPrice.value = details?.oneTimePurchaseOfferDetails?.formattedPrice
                onComplete?.invoke(details)
            } else {
                onComplete?.invoke(null)
            }
        }
    }

    /**
     * Launches the Google Play billing bottom sheet for 'remove_ads'.
     */
    fun launchBillingFlow(activity: Activity): Boolean {
        val details = _productDetails.value
        if (details == null) {
            queryProductDetails { loadedDetails ->
                if (loadedDetails != null) {
                    launchWithDetails(activity, loadedDetails)
                }
            }
            return false
        }
        return launchWithDetails(activity, details)
    }

    private fun launchWithDetails(activity: Activity, details: ProductDetails): Boolean {
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        val responseCode = billingClient.launchBillingFlow(activity, billingFlowParams).responseCode
        return responseCode == BillingClient.BillingResponseCode.OK
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { handlePurchase(it) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                // User cancelled purchase dialog
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                setAdsRemoved(true)
                queryExistingPurchases()
            }
            else -> {
                _statusMessage.value = "Purchase failed: ${billingResult.debugMessage}"
            }
        }
    }

    /**
     * Handles mandatory purchase acknowledgment.
     * Google Play automatically refunds any purchase unacknowledged after 3 days.
     */
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (purchase.products.contains(AdConfig.PRODUCT_REMOVE_ADS)) {
                setAdsRemoved(true)
            }

            // CRITICAL: Acknowledge purchase to prevent automatic 3-day refund
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { ackResult ->
                    if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        setAdsRemoved(true)
                    }
                }
            }
        }
    }

    /**
     * Queries Google Play for active purchases and restores non-consumables.
     */
    fun queryExistingPurchases(onFinished: ((Boolean) -> Unit)? = null) {
        if (!billingClient.isReady) {
            startConnection { queryExistingPurchases(onFinished) }
            return
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            var hasRemoveAds = false
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    if (purchase.products.contains(AdConfig.PRODUCT_REMOVE_ADS)) {
                        hasRemoveAds = true
                        handlePurchase(purchase)
                    }
                }
            }
            if (hasRemoveAds) {
                setAdsRemoved(true)
            }
            onFinished?.invoke(hasRemoveAds)
        }
    }

    private fun setAdsRemoved(removed: Boolean) {
        _isAdsRemoved.value = removed
        preferences.setAdsRemoved(removed)
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun destroy() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}
