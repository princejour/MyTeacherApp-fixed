package com.walhero.myteacher

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

class GoogleMobileAdsConsentManager(context: Context) {
    private val consentInformation =
        UserMessagingPlatform.getConsentInformation(context.applicationContext)

    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    val isPrivacyOptionsRequired: Boolean
        get() =
            consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun gatherConsent(
        activity: Activity,
        onConsentGatheringComplete: (FormError?) -> Unit
    ) {
        val params = ConsentRequestParameters.Builder().build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    onConsentGatheringComplete(formError)
                }
            },
            { requestConsentError ->
                onConsentGatheringComplete(requestConsentError)
            }
        )
    }

    fun showPrivacyOptionsForm(
        activity: Activity,
        onConsentFormDismissed: (FormError?) -> Unit
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            onConsentFormDismissed(formError)
        }
    }
}
