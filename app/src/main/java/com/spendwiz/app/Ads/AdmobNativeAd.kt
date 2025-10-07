package com.spendwiz.app.Ads

import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.spendwiz.app.R

@Composable
fun AdmobNativeAdCard() {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    LaunchedEffect(Unit) {
        val adLoader = AdLoader.Builder(context, context.getString(R.string.ad_unit_id_native_test))
            .forNativeAd { ad: NativeAd ->
                nativeAd?.destroy()
                nativeAd = ad
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("AdmobNativeAd", "❌ Failed to load native ad: ${error.message}")
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    DisposableEffect(Unit) {
        onDispose {
            nativeAd?.destroy()
            nativeAd = null
        }
    }

    nativeAd?.let { ad ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor =  MaterialTheme.colorScheme.surface
            )
        ) {
            AndroidView(
                factory = { ctx ->
                    val inflater = LayoutInflater.from(ctx)
                    val adView = inflater.inflate(R.layout.native_ad_view_for_insight_screen, null) as NativeAdView

                    // Map views
                    val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
                    headlineView.text = ad.headline
                    adView.headlineView = headlineView

                    val bodyView = adView.findViewById<TextView>(R.id.ad_body)
                    bodyView.text = ad.body
                    adView.bodyView = bodyView

                    val iconView = adView.findViewById<ImageView>(R.id.ad_icon)
                    ad.icon?.drawable?.let { iconView.setImageDrawable(it) }
                    adView.iconView = iconView

                    val ctaButton = adView.findViewById<Button>(R.id.ad_call_to_action)
                    ctaButton.text = ad.callToAction
                    adView.callToActionView = ctaButton

                    adView.setNativeAd(ad)
                    adView
                },
                update = {}
            )
        }
    }
}


