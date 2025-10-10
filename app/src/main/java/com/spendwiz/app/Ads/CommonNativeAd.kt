package com.spendwiz.app.Ads

import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.spendwiz.app.R

@Composable
fun CommonNativeAd(
    modifier: Modifier = Modifier,
    adUnitId: String,
) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var isAdLoading by remember { mutableStateOf(true) }

    val  adLayoutId: Int = R.layout.native_ad_view_for_insight_screen // Default layout

    // Use the adUnitId as a key for LaunchedEffect. If the ID changes, it will reload the ad.
    LaunchedEffect(adUnitId) {
        isAdLoading = true
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad: NativeAd ->
                nativeAd?.destroy() // Destroy any previous ad
                nativeAd = ad
                isAdLoading = false
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("CommonNativeAd", "❌ Failed to load native ad: ${error.message}")
                    nativeAd = null // Ensure no old ad is shown
                    isAdLoading = false
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    // This ensures the ad is destroyed when the composable leaves the screen
    DisposableEffect(adUnitId) {
        onDispose {
            nativeAd?.destroy()
            nativeAd = null
        }
    }

    // Only display the card if an ad has been successfully loaded
    nativeAd?.let { ad ->
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            AndroidView(
                factory = { ctx ->
                    // Inflate the custom layout
                    val adView = LayoutInflater.from(ctx)
                        .inflate(adLayoutId, null) as NativeAdView

                    // Map the views from the layout file.
                    // IMPORTANT: Your XML layout must contain views with these exact IDs.
                    val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
                    val bodyView = adView.findViewById<TextView>(R.id.ad_body)
                    val ctaButton = adView.findViewById<Button>(R.id.ad_call_to_action)

                    // Associate the ad assets with the views
                    headlineView.text = ad.headline
                    adView.headlineView = headlineView

                    bodyView.text = ad.body
                    adView.bodyView = bodyView

                    val mediaView = adView.findViewById<com.google.android.gms.ads.nativead.MediaView>(R.id.ad_media)
                    adView.mediaView = mediaView
                    if (ad.mediaContent != null) {
                        mediaView.mediaContent = ad.mediaContent
                    }


                    ctaButton.text = ad.callToAction
                    adView.callToActionView = ctaButton

                    // Register the NativeAdView to track impressions and clicks.
                    adView.setNativeAd(ad)

                    adView
                },
                // No update block needed for this static view
                update = {}
            )
        }
    }
}