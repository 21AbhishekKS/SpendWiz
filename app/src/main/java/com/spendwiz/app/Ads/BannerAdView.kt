package com.spendwiz.app.Ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
fun BannerAdView(modifier: Modifier = Modifier, adUnitId: String) {
    // Get the screen width to determine the ad size
    val screenWidth = LocalConfiguration.current.screenWidthDp

    // Use AndroidView to embed a classic Android View (AdView) in Compose.
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            // Create a new AdView
            AdView(context).apply {
                // Set the ad size. We are using an adaptive banner to fill the width.
                setAdSize(
                    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                        context,
                        screenWidth
                    )
                )
                // Set the ad unit ID.
                this.adUnitId = adUnitId

                // Build an ad request and load the ad.
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}