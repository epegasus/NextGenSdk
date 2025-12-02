package dev.pegasus.nextgensdk.ads.inter.engine

import android.app.Activity
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdPreloader
import dev.pegasus.nextgensdk.ads.inter.callbacks.InterstitialShowListener
import dev.pegasus.nextgensdk.ads.inter.storage.AdRegistry
import dev.pegasus.nextgensdk.ads.inter.utils.MainDispatcher

/**
 * Responsible for showing an available preloaded ad (via InterstitialAdPreloader.pollAd).
 * When ad is shown/dismissed/failed we notify listener and update registry state.
 */
internal class ShowEngine(
    private val registry: AdRegistry,
    private val preloadEngine: PreloadEngine
) {

    fun showAd(activity: Activity, adUnitId: String, listener: InterstitialShowListener?) {
        val ad: InterstitialAd? = try {
            InterstitialAdPreloader.pollAd(adUnitId)
        } catch (e: Exception) {
            null
        }

        if (ad == null) {
            MainDispatcher.run { listener?.onAdFailedToShow(adUnitId, "Ad not available") }
            return
        }

        ad.adEventCallback = object : InterstitialAdEventCallback {
            override fun onAdShowedFullScreenContent() {
                MainDispatcher.run { listener?.onAdShown(adUnitId) }
            }

            override fun onAdImpression() {
                // mark impression and (if bufferSize == null) stop preloading for that unit
                registry.markAdShown(adUnitId)
                MainDispatcher.run { listener?.onAdImpression(adUnitId) }
                MainDispatcher.run(300) { listener?.onAdImpressionDelay(adUnitId) }
                // if bufferSize is null, we should stop automatic reload
                registry.findAdKeyByUnit(adUnitId)?.let { key ->
                    val info = registry.getInfo(key)
                    if (info?.bufferSize == null) {
                        // stop preloader so SDK doesn't keep buffering automatically
                        preloadEngine.stopPreload(adUnitId)
                    }
                }
            }

            override fun onAdClicked() {
                MainDispatcher.run { listener?.onAdClicked(adUnitId) }
            }

            override fun onAdDismissedFullScreenContent() {
                // Ad consumed; remove mapping if it was single-shot (bufferSize==null)
                registry.findAdKeyByUnit(adUnitId)?.let { key ->
                    val info = registry.getInfo(key)
                    if (info?.bufferSize == null) {
                        registry.removePreload(adUnitId)
                    }
                }
                MainDispatcher.run { listener?.onAdDismissed(adUnitId) }
            }

            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                MainDispatcher.run { listener?.onAdFailedToShow(adUnitId, "code=${fullScreenContentError.code} msg=${fullScreenContentError.message}") }
                // On fail, if bufferSize == null, stop preloading
                registry.findAdKeyByUnit(adUnitId)?.let { key ->
                    val info = registry.getInfo(key)
                    if (info?.bufferSize == null) preloadEngine.stopPreload(adUnitId)
                }
            }
        }

        try {
            ad.show(activity)
        } catch (e: Exception) {
            MainDispatcher.run { listener?.onAdFailedToShow(adUnitId, e.message ?: "Exception showing ad") }
        }
    }
}