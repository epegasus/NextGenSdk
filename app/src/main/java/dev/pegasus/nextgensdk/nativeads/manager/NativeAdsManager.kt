package dev.pegasus.nextgensdk.nativeads.manager

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoadResult.NativeAdSuccess
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdPreloader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest
import dev.pegasus.nextgensdk.nativeads.callbacks.NativeOnLoadCallback
import dev.pegasus.nextgensdk.nativeads.callbacks.NativeOnShowCallback
import dev.pegasus.nextgensdk.utils.constants.Constants.TAG_ADS
import dev.pegasus.nextgensdk.utils.network.InternetManager
import dev.pegasus.nextgensdk.utils.storage.SharedPreferencesDataSource
import java.util.concurrent.ConcurrentHashMap

abstract class NativeAdsManager(
    protected val sharedPrefs: SharedPreferencesDataSource,
    protected val internetManager: InternetManager,
) {

    private val preloadStatusMap = ConcurrentHashMap<String, Boolean>()
    private val bufferSizeMap = ConcurrentHashMap<String, Int>()
    private val adShownMap = ConcurrentHashMap<String, Boolean>()
    private val mainHandler = Handler(Looper.getMainLooper())

    // Mirrors interstitial loadInterstitialAd (low‑level preload entry)
    protected fun loadNativeAd(
        adType: String,
        adUnitId: String,
        isRemoteEnable: Boolean,
        bufferSize: Int? = null,
        listener: NativeOnLoadCallback? = null
    ) {
        if (!isRemoteEnable) {
            logError(adType, "loadNativeAd", "Remote config is off")
            listener?.onResponse(false); return
        }
        if (sharedPrefs.isAppPurchased) {
            logError(adType, "loadNativeAd", "Premium user")
            listener?.onResponse(false); return
        }
        if (adUnitId.isBlank()) {
            logError(adType, "loadNativeAd", "Ad id is empty")
            listener?.onResponse(false); return
        }
        if (!internetManager.isInternetConnected) {
            logError(adType, "loadNativeAd", "Internet is not connected")
            listener?.onResponse(false); return
        }

        bufferSize?.let { bufferSizeMap[adUnitId] = it }

        if (preloadStatusMap[adUnitId] == true &&
            isAdAvailable(adUnitId)
        ) {
            logDebug(adType, "loadNativeAd", "Ad already available")
            listener?.onResponse(true); return
        }

        val preloadConfig = createPreloadConfig(adUnitId, bufferSize)
        logDebug(adType, "loadNativeAd", "Requesting server (bufferSize=$bufferSize)")

        val callback = object : PreloadCallback {
            override fun onAdPreloaded(preloadId: String, responseInfo: ResponseInfo) {
                logInfo(adType, "loadNativeAd", "onAdPreloaded: $preloadId")
                preloadStatusMap[adUnitId] = true
                postToMain { listener?.onResponse(true) }
            }

            override fun onAdFailedToPreload(preloadId: String, adError: LoadAdError) {
                logError(adType, "loadNativeAd", "failed: ${adError.message}")
                preloadStatusMap[adUnitId] = false
                // For non-buffered ads, clear preload on failure (same behavior as interstitials)
                if (!bufferSizeMap.containsKey(adUnitId)) {
                    stopPreloading(adUnitId)
                }
                postToMain { listener?.onResponse(false) }
            }

            override fun onAdsExhausted(preloadId: String) {
                logDebug(adType, "loadNativeAd", "onAdsExhausted: $preloadId")
            }
        }

        NativeAdPreloader.start(adUnitId, preloadConfig, callback)
        preloadStatusMap[adUnitId] = true
    }

    protected fun pollNativeAd(adType: String, adUnitId: String, listener: NativeOnShowCallback?): NativeAd? {
        return pollAd(adUnitId)?.let { ad ->
            logDebug(adType, "pollNativeAd", "got ad, responseInfo=${ad.getResponseInfo().responseId}")

            ad.adEventCallback = object : NativeAdEventCallback {
                override fun onAdImpression() {
                    logVerbose(adType, "pollNativeAd", "onAdImpression")
                    adShownMap[adUnitId] = true
                    postToMain { listener?.onAdImpression() }
                    postToMainDelayed { listener?.onAdImpressionDelayed() }
                    // For non-buffered ads, clear preload after first impression
                    if (!bufferSizeMap.containsKey(adUnitId)) {
                        stopPreloading(adUnitId)
                    }
                }

                override fun onAdClicked() {
                    logDebug(adType, "pollNativeAd", "onAdClicked")
                    postToMain { listener?.onAdClicked() }
                }

                override fun onAdPaid(value: AdValue) {
                    logDebug(adType, "pollNativeAd", "onPaid ${value.valueMicros} ${value.currencyCode}")
                }
            }
            ad
        } ?: run {
            logError(adType, "pollNativeAd", "no ad available")
            null
        }
    }

    protected fun wasNativeShown(adUnitId: String): Boolean =
        adShownMap[adUnitId] == true

    /** Mirror interstitial stopPreloading: destroy + clear all maps for this adUnitId. */
    open fun stopPreloading(adUnitId: String) {
        try {
            destroyPreload(adUnitId)
            preloadStatusMap.remove(adUnitId)
            bufferSizeMap.remove(adUnitId)
            adShownMap.remove(adUnitId)
        } catch (e: Exception) {
            logError("", "stopPreloading (native)", "Exception: ${e.message}")
        }
    }

    fun isPreloadActive(adUnitId: String): Boolean = preloadStatusMap[adUnitId] == true

    fun clearAllNative() {
        preloadStatusMap.clear()
        bufferSizeMap.clear()
        adShownMap.clear()
        // NativeAdPreloader has no destroyAll() API in sample; we just clear our state.
    }

    // Private helper methods (mirroring interstitial manager)
    private fun createPreloadConfig(adUnitId: String, bufferSize: Int?): PreloadConfiguration {
        val request = NativeAdRequest.Builder(adUnitId, listOf(NativeAd.NativeAdType.NATIVE)).build()
        val size = bufferSize?.takeIf { it > 0 } ?: 1
        return PreloadConfiguration(request, size)
    }

    private fun isAdAvailable(adUnitId: String): Boolean =
        NativeAdPreloader.isAdAvailable(adUnitId)

    private fun pollAd(adUnitId: String): NativeAd? {
        val result = NativeAdPreloader.pollAd(adUnitId)
        return if (result is NativeAdSuccess) result.ad else null
    }

    private fun destroyPreload(adUnitId: String) {
        NativeAdPreloader.destroy(adUnitId)
    }

    private fun postToMain(action: () -> Unit) = mainHandler.post(action)

    private fun postToMainDelayed(delayMillis: Long = 300, action: () -> Unit) =
        mainHandler.postDelayed(action, delayMillis)

    // Logging helpers (mirroring interstitial manager)
    private fun logError(adType: String, method: String, message: String) {
        Log.e(TAG_ADS, "$adType -> $method: $message")
    }

    private fun logDebug(adType: String, method: String, message: String) {
        Log.d(TAG_ADS, "$adType -> $method: $message")
    }

    private fun logInfo(adType: String, method: String, message: String) {
        Log.i(TAG_ADS, "$adType -> $method: $message")
    }

    private fun logVerbose(adType: String, method: String, message: String) {
        Log.v(TAG_ADS, "$adType -> $method: $message")
    }
}