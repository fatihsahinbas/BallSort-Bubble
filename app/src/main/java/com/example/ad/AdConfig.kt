package com.example.ad

/**
 * AdMob and In-App Billing Configuration.
 * 
 * ====================================================================================
 * IMPORTANT: TEST AD UNIT IDs ARE USED BY DEFAULT FOR SAFE DEVELOPMENT AND TESTING.
 * BEFORE RELEASING TO GOOGLE PLAY:
 * Replace the sample IDs below with your real AdMob App ID and Ad Unit IDs from
 * your Google AdMob console (https://admob.google.com).
 * ====================================================================================
 */
object AdConfig {
    // AdMob Sample Test Application ID (Must also match AndroidManifest.xml meta-data)
    // Production: Replace with ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX
    const val ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"

    // AdMob Test Banner Ad Unit ID
    // Production: Replace with ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    // AdMob Test Interstitial Ad Unit ID
    // Production: Replace with ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    // AdMob Test Rewarded Video Ad Unit ID
    // Production: Replace with ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    // Google Play Console In-App Product ID (Non-consumable)
    // Create this In-App Product in Google Play Console -> Monetization -> Products -> In-app products
    const val PRODUCT_REMOVE_ADS = "remove_ads"

    // Interstitial frequency: show after every N completed levels
    const val INTERSTITIAL_LEVEL_INTERVAL = 4
}
