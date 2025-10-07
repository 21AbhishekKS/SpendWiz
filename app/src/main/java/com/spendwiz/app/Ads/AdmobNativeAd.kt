package com.spendwiz.app.Ads

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions

// Use the official test ID for native ads
const val NATIVE_AD_TEST_ID = "ca-app-pub-3940256099942544/2247696110"

@Composable
fun AdmobNativeAd() {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var adFailedToLoad by remember { mutableStateOf(false) }

    // This effect will load the ad when the composable enters the composition
    LaunchedEffect(Unit) {
        val adLoader = AdLoader.Builder(context, NATIVE_AD_TEST_ID)
            .forNativeAd { ad: NativeAd ->
                // Ad loaded successfully, update the state
                nativeAd = ad
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    // Mark as failed to prevent retries and hide the ad space
                    adFailedToLoad = true
                    Log.e("AdmobNativeAd", "Ad failed to load: ${loadAdError.message}")
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    // Clean up the ad when the composable is removed from the screen
    DisposableEffect(Unit) {
        onDispose {
            nativeAd?.destroy()
        }
    }

    // If the ad is loaded, display it using your custom NativeAdCard
    // If it's null (loading or failed), this will compose nothing, achieving your goal.
    nativeAd?.let { ad ->
        val iconPainter = ad.icon?.drawable?.let { rememberDrawablePainter(drawable = it) }

        // We only show the ad if the icon is not null
        if (iconPainter != null) {
            NativeAdCard(
                headline = ad.headline ?: "Default Headline",
                body = ad.body ?: "This is a default ad body.",
                callToAction = ad.callToAction ?: "Learn More",
                icon = iconPainter,
                advertiser = ad.advertiser ?: "Advertiser"
            )
        }
    }
}