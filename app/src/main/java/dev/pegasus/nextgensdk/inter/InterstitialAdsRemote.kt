package dev.pegasus.nextgensdk.inter

import android.app.Activity
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdPreloader
import dev.pegasus.nextgensdk.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class InterstitialAdsRemote {

    fun loadInterstitialAd(key: String, adUnitId: String): Flow<Boolean> = callbackFlow {

        val preloadCallback = object : PreloadCallback {
            override fun onAdPreloaded(preloadId: String, responseInfo: ResponseInfo) {
                Log.d(Constants.TAG_ADS, "$key -> loadInterstitialAd: onAdPreloaded: preloadId: $preloadId")
                trySend(true)
                close()
            }

            override fun onAdFailedToPreload(preloadId: String, adError: LoadAdError) {
                Log.e(Constants.TAG_ADS, "$key -> loadInterstitialAd: onAdFailedToPreload: preloadId: $preloadId, adMessage: ${adError.message}")
                trySend(false)
                close()
            }

            override fun onAdsExhausted(preloadId: String) {
                Log.d(Constants.TAG_ADS, "$key -> loadInterstitialAd: onAdsExhausted: preloadId: $preloadId")
            }
        }

        val adRequest = AdRequest.Builder(adUnitId).build()
        val preloadConfig = PreloadConfiguration(adRequest)
        val isIdInUse = InterstitialAdPreloader.start(adUnitId, preloadConfig, preloadCallback)
        Log.d(Constants.TAG_ADS, "$key -> loadInterstitial: Is preloaded started: $isIdInUse or preloadId is in-use: ${!isIdInUse}")
        awaitClose { }
    }

    fun showInterstitialAd(activity: Activity?, key: String, adUnitId: String) {
        // Polling returns the next available ad and loads another ad in the background
        val ad = InterstitialAdPreloader.pollAd(adUnitId)
        ad?.adEventCallback = object : InterstitialAdEventCallback {
            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                Log.d(Constants.TAG_ADS, "$key -> showInterstitialAd: onAdShowedFullScreenContent: called")
            }

            override fun onAdImpression() {
                super.onAdImpression()
                Log.d(Constants.TAG_ADS, "$key -> showInterstitialAd: onAdImpression: called")
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                Log.d(Constants.TAG_ADS, "$key -> showInterstitialAd: onAdDismissedFullScreenContent: called")
            }

            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                super.onAdFailedToShowFullScreenContent(fullScreenContentError)
                Log.e(Constants.TAG_ADS, "$key -> showInterstitialAd: onAdFailedToShowFullScreenContent: ${fullScreenContentError.code} -- ${fullScreenContentError.message}")
            }
        }
        activity?.let { ad?.show(it) } ?: Log.e(Constants.TAG_ADS, "$key -> showInterstitialAd: activity is null")
    }
}