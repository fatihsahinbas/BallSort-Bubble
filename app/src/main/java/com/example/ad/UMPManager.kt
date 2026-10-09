package com.example.ad

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * UMPManager handles the Google User Messaging Platform (UMP) consent flow.
 *
 * Ensures full adherence to GDPR, EEA, and Google Play ad policies:
 * - Checks user consent status on app launch.
 * - Presents the GDPR/EEA consent form if required.
 * - Ensures AdMob MobileAds is ONLY initialized and loaded after valid user consent is obtained.
 * - Exposes a privacy options form so users can review or change consent anytime from Settings.
 */
class UMPManager(private val context: Context) {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    private val _canRequestAdsState = MutableStateFlow(consentInformation.canRequestAds())
    val canRequestAdsState: StateFlow<Boolean> = _canRequestAdsState.asStateFlow()

    /**
     * Checks if ads can be requested based on the current consent status.
     * Ads should ONLY be initialized or loaded when this returns true.
     */
    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    /**
     * Indicates whether the user needs to be shown the privacy options form in Settings.
     */
    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /**
     * Gathers user consent on app launch.
     * Requests consent info updates and automatically displays the form if needed.
     *
     * @param activity The current foreground Activity
     * @param onConsentGathered Callback invoked with whether ads can now be requested
     */
    fun gatherConsent(
        activity: Activity,
        onConsentGathered: (canRequestAds: Boolean) -> Unit
    ) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError: FormError? ->
                    val allowed = consentInformation.canRequestAds()
                    _canRequestAdsState.value = allowed
                    onConsentGathered(allowed)
                }
            },
            { requestConsentError: FormError ->
                // In case of request failure, determine if cached consent allows ad requests
                val allowed = consentInformation.canRequestAds()
                _canRequestAdsState.value = allowed
                onConsentGathered(allowed)
            }
        )
    }

    /**
     * Overload for backward compatibility with requestConsent.
     */
    fun requestConsent(activity: Activity, onComplete: () -> Unit) {
        gatherConsent(activity) { onComplete() }
    }

    /**
     * Re-opens the privacy options form so users can update their consent preferences anytime.
     */
    fun showPrivacyOptionsForm(
        activity: Activity,
        onDismissed: ((FormError?) -> Unit)? = null
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError: FormError? ->
            _canRequestAdsState.value = consentInformation.canRequestAds()
            onDismissed?.invoke(formError)
        }
    }

    /**
     * Resets consent state for debug and testing.
     */
    fun resetConsentForTesting() {
        consentInformation.reset()
        _canRequestAdsState.value = consentInformation.canRequestAds()
    }
}

/**
 * Type alias to maintain backwards compatibility across all callers.
 */
typealias ConsentManager = UMPManager
