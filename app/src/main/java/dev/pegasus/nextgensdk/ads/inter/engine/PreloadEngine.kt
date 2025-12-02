package dev.pegasus.nextgensdk.ads.inter.engine


import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdPreloader
import dev.pegasus.nextgensdk.ads.inter.callbacks.InterstitialLoadListener
import dev.pegasus.nextgensdk.ads.inter.model.AdInfo
import dev.pegasus.nextgensdk.ads.inter.storage.AdRegistry
import dev.pegasus.nextgensdk.ads.inter.utils.MainDispatcher
import dev.pegasus.nextgensdk.utils.constants.Constants.TAG_ADS

/**
 * Uses Next-Gen SDK's InterstitialAdPreloader and hands back load events.
 * Buffering is delegated to the SDK via PreloadConfiguration.bufferSize.
 */
internal class PreloadEngine(
    private val registry: AdRegistry
) {

    /**
     * Start preloading an ad for a given AdInfo.
     * If bufferSize is null -> we still start preloader with size=1, but we will stop preloading
     * after the impression (registry.markAdShown -> manager will call stop).
     *
     * If an adUnit is already preloading, we will not start duplicate preloader.
     */
    fun startPreload(adInfo: AdInfo, listener: InterstitialLoadListener?) {
        val adUnitId = adInfo.adUnitId

        // avoid duplicate start
        if (registry.isPreloadActive(adUnitId)) {
            Log.d(TAG_ADS, "PreloadEngine.startPreload: already active for $adUnitId")
            MainDispatcher.run { listener?.onLoaded(adUnitId) } // if SDK already loaded, reply true
            return
        }

        registry.markPreloadActive(adUnitId, true)
        val buffer = adInfo.bufferSize ?: 1 // pass 1 to SDK if null; we'll stop after impression manually

        val request = AdRequest.Builder(adUnitId).build()
        val config = PreloadConfiguration(request, buffer)

        try {
            val started = InterstitialAdPreloader.start(adUnitId, config, object : PreloadCallback {
                override fun onAdPreloaded(preloadId: String, responseInfo: ResponseInfo) {
                    Log.d(TAG_ADS, "onAdPreloaded: $preloadId")
                    registry.markPreloadActive(adUnitId, true)
                    MainDispatcher.run { listener?.onLoaded(adUnitId) }
                }

                override fun onAdFailedToPreload(preloadId: String, adError: LoadAdError) {
                    Log.e(TAG_ADS, "onAdFailedToPreload: $preloadId -> ${adError.message}")
                    registry.markPreloadActive(adUnitId, false)
                    // if adInfo.bufferSize == null we might want to remove the preload (already not active)
                    MainDispatcher.run { listener?.onFailed(adUnitId, adError.message) }
                }

                override fun onAdsExhausted(preloadId: String) {
                    // SDK-level events; we don't act specifically here, but could notify metrics
                    Log.v(TAG_ADS, "onAdsExhausted: $preloadId")
                }
            })

            if (!started) {
                // Another preloader for same id is already in place. Mark as active and notify.
                Log.d(TAG_ADS, "InterstitialAdPreloader.start returned false (id already in use): $adUnitId")
                registry.markPreloadActive(adUnitId, true)
                MainDispatcher.run { listener?.onLoaded(adUnitId) }
            }
        } catch (e: Exception) {
            registry.markPreloadActive(adUnitId, false)
            Log.e(TAG_ADS, "PreloadEngine.startPreload exception: ${e.message}")
            MainDispatcher.run { listener?.onFailed(adUnitId, e.message ?: "Exception") }
        }
    }

    /**
     * Stop preloading/destroy preloader for a given unit id.
     */
    fun stopPreload(adUnitId: String) {
        try {
            InterstitialAdPreloader.destroy(adUnitId)
        } catch (e: Exception) {
            Log.e(TAG_ADS, "PreloadEngine.stopPreload exception: ${e.message}")
        } finally {
            registry.removePreload(adUnitId)
        }
    }

    fun stopAll() {
        // Not a direct SDK call for listing preloads; we iterate known registry entries.
        registry.clearAll()
    }
}