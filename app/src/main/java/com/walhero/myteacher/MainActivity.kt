package com.walhero.myteacher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class MainActivity : ComponentActivity() {
    private lateinit var consentManager: GoogleMobileAdsConsentManager
    private var isMobileAdsInitializeCalled = false
    private var isRewardedAdLoading = false
    private var rewardedAd: RewardedAd? = null

    private var canRequestAds by mutableStateOf(false)
    private var isRewardedAdReady by mutableStateOf(false)
    private var isPrivacyOptionsRequired by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        consentManager = GoogleMobileAdsConsentManager(applicationContext)

        setContent {
            MaterialTheme {
                MyTeacherApp(
                    canRequestAds = canRequestAds,
                    isRewardedAdReady = isRewardedAdReady,
                    isPrivacyOptionsRequired = isPrivacyOptionsRequired,
                    onPrivacyOptionsClick = ::showPrivacyOptionsForm,
                    onPrivacyPolicyClick = ::openPrivacyPolicy,
                    onShowRewardedAd = ::showRewardedAd
                )
            }
        }

        gatherConsent()
    }

    private fun gatherConsent() {
        consentManager.gatherConsent(this) { consentError ->
            consentError?.let {
                Log.w(TAG, "${it.errorCode}: ${it.message}")
            }

            updatePrivacyOptionsRequirement()
            if (consentManager.canRequestAds) {
                initializeMobileAds()
            }
        }

        if (consentManager.canRequestAds) {
            initializeMobileAds()
        }
    }

    private fun updatePrivacyOptionsRequirement() {
        isPrivacyOptionsRequired = consentManager.isPrivacyOptionsRequired
    }

    private fun initializeMobileAds() {
        if (isMobileAdsInitializeCalled) {
            canRequestAds = true
            loadRewardedAd()
            return
        }

        isMobileAdsInitializeCalled = true
        MobileAds.initialize(this) {
            runOnUiThread {
                canRequestAds = true
                loadRewardedAd()
            }
        }
    }

    private fun loadRewardedAd() {
        if (!canRequestAds || isRewardedAdLoading || rewardedAd != null) return

        isRewardedAdLoading = true
        isRewardedAdReady = false

        RewardedAd.load(
            this,
            BuildConfig.ADMOB_REWARDED_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedAdLoading = false
                    isRewardedAdReady = true
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardedAd = null
                    isRewardedAdLoading = false
                    isRewardedAdReady = false
                    Log.w(TAG, "Rewarded ad failed to load: ${adError.message}")
                }
            }
        )
    }

    private fun showRewardedAd(
        onRewardEarned: () -> Unit,
        onUnavailable: (String) -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null) {
            loadRewardedAd()
            onUnavailable("The rewarded ad is not ready yet. Please try again shortly.")
            return
        }

        rewardedAd = null
        isRewardedAdReady = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                loadRewardedAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Rewarded ad failed to show: ${adError.message}")
                onUnavailable("The rewarded ad could not be shown. Please try again.")
                loadRewardedAd()
            }
        }

        ad.show(this) {
            onRewardEarned()
        }
    }

    private fun showPrivacyOptionsForm() {
        consentManager.showPrivacyOptionsForm(this) { formError ->
            formError?.let {
                Log.w(TAG, "${it.errorCode}: ${it.message}")
            }

            updatePrivacyOptionsRequirement()
            if (consentManager.canRequestAds) {
                initializeMobileAds()
            }
        }
    }

    private fun openPrivacyPolicy() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.PRIVACY_POLICY_URL)))
    }

    private companion object {
        const val TAG = "MyTeacherAdMob"
    }
}
