package com.spendwiz.app.Ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object InterstitialAdManager {

    private const val TAG = "InterstitialAdManager"
    private const val AD_UNIT_ID = "ca-app-pub-6662174152116703/5039177136"
    private const val MIN_SESSION_DURATION_MS = 180000L // 3 minute in milliseconds

    private var mInterstitialAd: InterstitialAd? = null
    private var isAdLoading = false
    private var appStartTime: Long = 0L

    /**
     * Checks if the session timer has started, and starts it if it hasn't.
     * This makes initialization automatic on first use.
     */
    private fun checkAndStartTimer() {
        if (appStartTime == 0L) {
            appStartTime = System.currentTimeMillis()
            Log.d(TAG, "InterstitialAdManager session timer started on first use.")
        }
    }

    /**
     * Checks if the user has been in the app for more than the minimum duration.
     * @return true if the session duration is sufficient, false otherwise.
     */
    private fun isSessionDurationSufficient(): Boolean {
        // If the timer hasn't started, the duration is definitely not sufficient.
        if (appStartTime == 0L) {
            return false
        }
        val elapsedTime = System.currentTimeMillis() - appStartTime
        return elapsedTime > MIN_SESSION_DURATION_MS
    }

    fun loadAd(context: Context) {
        // Automatically start the timer on the first call to this function.
        checkAndStartTimer()

        // Condition 1: Check if enough time has passed.
        if (!isSessionDurationSufficient()) {
            Log.d(TAG, "Not loading ad, session duration is less than 3 minute.")
            return
        }

        // Condition 2: Avoid loading a new ad if one is already loading or has been loaded.
        if (isAdLoading || mInterstitialAd != null) {
            return
        }

        isAdLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    isAdLoading = false
                    Log.i(TAG, "Interstitial ad loaded successfully.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                    mInterstitialAd = null
                    isAdLoading = false
                }
            }
        )
    }

    fun showAd(activity: Activity, onAdDismissed: () -> Unit) {
        // Automatically start the timer on the first call to this function.
        checkAndStartTimer()

        // Check if enough time has passed before showing the ad.
        if (!isSessionDurationSufficient()) {
            Log.d(TAG, "Not showing ad, session duration is less than 3 minute.")
            onAdDismissed() // Proceed with the app flow without showing the ad.
            return
        }

        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad was dismissed.")
                    // The ad is now invalid, so we nullify it to allow the next one to be loaded.
                    mInterstitialAd = null
                    // Pre-load the next ad for future use (it will respect the time condition).
                    loadAd(activity)
                    // Perform the action after the ad is dismissed.
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Ad failed to show: ${adError.message}")
                    mInterstitialAd = null
                    // Still perform the action if the ad fails to show, so the user is not stuck.
                    onAdDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed fullscreen content.")
                }
            }
            mInterstitialAd?.show(activity)
        } else {
            Log.d(TAG, "The interstitial ad wasn't ready yet. Loading one for next time.")
            // Attempt to load an ad for the next time, since the time condition has been met.
            loadAd(activity)
            // If the ad isn't ready, don't block the user.
            // Immediately perform the action (e.g., navigation).
            onAdDismissed()
        }
    }
}

