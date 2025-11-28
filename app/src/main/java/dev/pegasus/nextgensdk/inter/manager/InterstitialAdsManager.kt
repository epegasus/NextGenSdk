package dev.pegasus.nextgensdk.inter.manager

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdPreloader
import dev.pegasus.nextgensdk.inter.callbacks.InterstitialOnLoadCallBack
import dev.pegasus.nextgensdk.inter.callbacks.InterstitialOnShowCallBack
import dev.pegasus.nextgensdk.utils.constants.Constants.TAG_ADS
import dev.pegasus.nextgensdk.utils.network.InternetManager
import dev.pegasus.nextgensdk.utils.storage.SharedPreferencesDataSource
import java.util.concurrent.ConcurrentHashMap

abstract class InterstitialAdsManager(
    protected val sharedPreferencesDataSource: SharedPreferencesDataSource,
    protected val internetManager: InternetManager
) {

    private val preloadStatusMap = ConcurrentHashMap<String, Boolean>()
    private val bufferSizeMap = ConcurrentHashMap<String, Int>()
    private val adShownMap = ConcurrentHashMap<String, Boolean>()
    private val mainHandler = Handler(Looper.getMainLooper())

    protected fun loadInterstitialAd(
        adType: String,
        adUnitId: String,
        isRemoteEnable: Boolean,
        bufferSize: Int?,
        listener: InterstitialOnLoadCallBack? = null
    ) {
        // Validation checks
        when {
            !isRemoteEnable -> {
                logError(adType, "loadInterstitialAd", "Remote config is off")
                listener?.onResponse(false)
                return
            }
            sharedPreferencesDataSource.isAppPurchased -> {
                logError(adType, "loadInterstitialAd", "Premium user")
                listener?.onResponse(false)
                return
            }
            adUnitId.trim().isEmpty() -> {
                logError(adType, "loadInterstitialAd", "Ad id is empty")
                listener?.onResponse(false)
                return
            }
            !internetManager.isInternetConnected -> {
                logError(adType, "loadInterstitialAd", "Internet is not connected")
                listener?.onResponse(false)
                return
            }
        }

        bufferSize?.let { bufferSizeMap[adUnitId] = it }

        // Check if already loading or available
        when {
            preloadStatusMap[adUnitId] == true -> {
                logDebug(adType, "loadInterstitialAd", "Ad is already loading for this ad unit: $adUnitId")
                listener?.onResponse(isAdAvailable(adUnitId))
                return
            }
            isAdAvailable(adUnitId) -> {
                logInfo(adType, "loadInterstitialAd", "Ad already available")
                preloadStatusMap[adUnitId] = true
                listener?.onResponse(true)
                return
            }
        }

        logDebug(adType, "loadInterstitialAd", "Requesting admob server for ad...")

        try {
            val preloadConfig = createPreloadConfig(adUnitId, bufferSize)
            val isIdInUse = InterstitialAdPreloader.start(adUnitId, preloadConfig, createPreloadCallback(adType, adUnitId, bufferSize, listener))

            if (!isIdInUse) {
                logDebug(adType, "loadInterstitialAd", "AdUnitId is already in use")
                preloadStatusMap[adUnitId] = true
                listener?.onResponse(isAdAvailable(adUnitId))
            } else {
                preloadStatusMap[adUnitId] = true
            }
        } catch (e: Exception) {
            logError(adType, "loadInterstitialAd", "Exception: ${e.message}")
            preloadStatusMap[adUnitId] = false
            listener?.onResponse(false)
        }
    }

    protected fun showInterstitialAd(
        activity: Activity?,
        adType: String,
        adUnitId: String,
        listener: InterstitialOnShowCallBack?
    ) {
        // Validation checks
        when {
            sharedPreferencesDataSource.isAppPurchased -> {
                logError(adType, "showInterstitialAd", "Premium user")
                listener?.onAdFailedToShow()
                return
            }
            !isAdAvailable(adUnitId) -> {
                logError(adType, "showInterstitialAd", "Interstitial is not available yet")
                listener?.onAdFailedToShow()
                return
            }
            activity == null -> {
                logError(adType, "showInterstitialAd", "activity reference is null")
                listener?.onAdFailedToShow()
                return
            }
            activity.isFinishing || activity.isDestroyed -> {
                logError(adType, "showInterstitialAd", "activity is finishing or destroyed")
                listener?.onAdFailedToShow()
                return
            }
            adUnitId.trim().isEmpty() -> {
                logError(adType, "showInterstitialAd", "Ad id is empty")
                listener?.onAdFailedToShow()
                return
            }
        }

        val ad = pollAd(adUnitId) ?: run {
            logError(adType, "showInterstitialAd", "Failed to poll ad")
            listener?.onAdFailedToShow()
            return
        }

        logDebug(adType, "showInterstitialAd", "showing ad")
        ad.adEventCallback = createAdEventCallback(adType, adUnitId, bufferSizeMap.containsKey(adUnitId), listener)

        try {
            ad.show(activity)
        } catch (e: Exception) {
            logError(adType, "showInterstitialAd", "Exception showing ad: ${e.message}")
            listener?.onAdFailedToShow()
        }
    }

    fun isInterstitialAvailable(adUnitId: String) = isAdAvailable(adUnitId)

    open fun stopPreloading(adUnitId: String) {
        try {
            destroyPreload(adUnitId)
            preloadStatusMap.remove(adUnitId)
            bufferSizeMap.remove(adUnitId)
            adShownMap.remove(adUnitId)
        } catch (e: Exception) {
            logError("", "stopPreloading", "Exception: ${e.message}")
        }
    }

    fun isPreloadActive(adUnitId: String) = preloadStatusMap[adUnitId] == true

    protected fun wasAdShown(adUnitId: String) = adShownMap[adUnitId] == true

    fun clearAll() {
        preloadStatusMap.clear()
        bufferSizeMap.clear()
        adShownMap.clear()
        mainHandler.removeCallbacksAndMessages(null)
    }

    // Private helper methods
    private fun createPreloadConfig(adUnitId: String, bufferSize: Int?): PreloadConfiguration {
        val adRequest = AdRequest.Builder(adUnitId).build()
        val size = bufferSize?.takeIf { it > 0 } ?: 1
        return PreloadConfiguration(adRequest, size)
    }

    private fun isAdAvailable(adUnitId: String) = InterstitialAdPreloader.isAdAvailable(adUnitId)

    private fun pollAd(adUnitId: String) = InterstitialAdPreloader.pollAd(adUnitId)

    private fun destroyPreload(adUnitId: String) = InterstitialAdPreloader.destroy(adUnitId)

    private fun postToMain(action: () -> Unit) = mainHandler.post(action)

    private fun postToMainDelayed(delayMillis: Long = 300, action: () -> Unit) = mainHandler.postDelayed(action, delayMillis)

    private fun createPreloadCallback(
        adType: String,
        adUnitId: String,
        bufferSize: Int?,
        listener: InterstitialOnLoadCallBack?
    ) = object : PreloadCallback {
        override fun onAdPreloaded(preloadId: String, responseInfo: ResponseInfo) {
            logInfo(adType, "loadInterstitialAd", "onAdLoaded: adUnitId: $preloadId")
            preloadStatusMap[adUnitId] = true
            postToMain { listener?.onResponse(true) }
        }

        override fun onAdFailedToPreload(preloadId: String, adError: LoadAdError) {
            logError(adType, "loadInterstitialAd", "onAdFailedToLoad: adUnitId: $preloadId, adMessage: ${adError.message}")
            preloadStatusMap[adUnitId] = false
            if (bufferSize == null) {
                stopPreloading(adUnitId)
            }
            postToMain { listener?.onResponse(false) }
        }

        override fun onAdsExhausted(preloadId: String) {
            // No-op
        }
    }

    private fun createAdEventCallback(
        adType: String,
        adUnitId: String,
        hasBufferSize: Boolean,
        listener: InterstitialOnShowCallBack?
    ) = object : InterstitialAdEventCallback {
        override fun onAdShowedFullScreenContent() {
            super.onAdShowedFullScreenContent()
            postToMain { listener?.onAdShowedFullScreenContent() }
        }

        override fun onAdImpression() {
            super.onAdImpression()
            logVerbose(adType, "showInterstitialAd", "onAdImpression: called")
            adShownMap[adUnitId] = true
            postToMain { listener?.onAdImpression() }
            postToMainDelayed { listener?.onAdImpressionDelayed() }
            if (!hasBufferSize) {
                stopPreloading(adUnitId)
            }
        }

        override fun onAdDismissedFullScreenContent() {
            super.onAdDismissedFullScreenContent()
            logDebug(adType, "showInterstitialAd", "onAdDismissedFullScreenContent: called")
            postToMain { listener?.onAdDismissedFullScreenContent() }
        }

        override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
            super.onAdFailedToShowFullScreenContent(fullScreenContentError)
            logError(adType, "showInterstitialAd", "onAdFailedToShowFullScreenContent: ${fullScreenContentError.code} -- ${fullScreenContentError.message}")
            if (!hasBufferSize) {
                stopPreloading(adUnitId)
            }
            postToMain { listener?.onAdFailedToShow() }
        }

        override fun onAdClicked() {
            super.onAdClicked()
            logDebug(adType, "showInterstitialAd", "onAdClicked: called")
            postToMain { listener?.onAdClicked() }
        }

        override fun onAdPaid(value: AdValue) {
            super.onAdPaid(value)
            // No-op (can add logging if needed)
        }
    }

    // Logging helpers
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
