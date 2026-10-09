package com.example.ad

/**
 * AdMob and In-App Billing Configuration.
 *
 * ==============================================================================================
 * IMPORTANT: GOOGLE SAMPLE TEST AD UNIT IDs ARE CONFIGURED BY DEFAULT.
 * This guarantees safe development, prevents invalid traffic strikes on your AdMob account,
 * and allows realistic ad rendering during testing on emulators and test devices.
 *
 * BEFORE RELEASING TO GOOGLE PLAY STORE:
 * 1. Log in to Google AdMob Console: https://admob.google.com
 * 2. Create an App entry and generate Ad Units for Banner, Interstitial, and Rewarded Video.
 * 3. Replace the placeholder IDs below with your real AdMob App ID and Ad Unit IDs.
 * 4. Also update the `com.google.android.gms.ads.APPLICATION_ID` meta-data in `AndroidManifest.xml`.
 * ==============================================================================================
 */
object AdConfig {

    /**
     * AdMob App ID.
     * Official Google Sample App ID: ca-app-pub-3940256099942544~3347511713
     *
     * [ACTION REQUIRED BEFORE PRODUCTION RELEASE]:
     * Replace with your production App ID from AdMob Console:
     * Format: "ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX"
     * NOTE: You MUST also update the matching value in app/src/main/AndroidManifest.xml!
     */
    const val ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"

    /**
     * AdMob Adaptive Banner Ad Unit ID.
     * Official Google Test Banner ID: ca-app-pub-3940256099942544/6300978111
     *
     * [ACTION REQUIRED BEFORE PRODUCTION RELEASE]:
     * Replace with your production Banner Ad Unit ID from AdMob Console:
     * Format: "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
     */
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    /**
     * AdMob Interstitial Ad Unit ID.
     * Official Google Test Interstitial ID: ca-app-pub-3940256099942544/1033173712
     *
     * [ACTION REQUIRED BEFORE PRODUCTION RELEASE]:
     * Replace with your production Interstitial Ad Unit ID from AdMob Console:
     * Format: "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
     */
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    /**
     * AdMob Rewarded Video Ad Unit ID.
     * Official Google Test Rewarded ID: ca-app-pub-3940256099942544/5224354917
     *
     * [ACTION REQUIRED BEFORE PRODUCTION RELEASE]:
     * Replace with your production Rewarded Ad Unit ID from AdMob Console:
     * Format: "ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX"
     */
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    /**
     * Google Play Billing Product ID for removing ads (Non-consumable one-time purchase).
     * Must match the Product ID registered in Google Play Console:
     * Monetize -> Products -> In-app products -> "remove_ads"
     */
    const val PRODUCT_REMOVE_ADS = "remove_ads"

    /**
     * Interstitial Frequency Cap:
     * Shows a full-screen interstitial ad after every N completed levels.
     * Default: 4 levels (prevents ad fatigue).
     */
    const val INTERSTITIAL_LEVEL_INTERVAL = 4
}
