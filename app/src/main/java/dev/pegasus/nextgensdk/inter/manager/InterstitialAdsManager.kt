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

    private fun postToMain(action: () -> Unit) {
        mainHandler.post(action)
    }

    private fun postToMainDelayed(delayMillis: Long = 300, action: () -> Unit) {
        mainHandler.postDelayed(action, delayMillis)
    }

    private fun createPreloadConfig(adUnitId: String, bufferSize: Int? = null): PreloadConfiguration {
        val adRequest = AdRequest.Builder(adUnitId).build()
        val size = if (bufferSize == null || bufferSize < 0) 1 else bufferSize
        return PreloadConfiguration(adRequest, size)
    }

    private fun isAdAvailable(adUnitId: String): Boolean {
        return InterstitialAdPreloader.isAdAvailable(adUnitId)
    }

    private fun pollAd(adUnitId: String): InterstitialAd? {
        return InterstitialAdPreloader.pollAd(adUnitId)
    }

    private fun destroyPreload(adUnitId: String) {
        InterstitialAdPreloader.destroy(adUnitId)
    }

    protected fun startPreloading(
        adType: String,
        adUnitId: String,
        isRemoteEnable: Boolean,
        bufferSize: Int?,
        listener: InterstitialOnLoadCallBack? = null
    ) {
        if (!isRemoteEnable) {
            Log.e(TAG_ADS, "$adType -> startPreloading: Remote config is off")
            listener?.onResponse(false)
            return
        }

        if (sharedPreferencesDataSource.isAppPurchased) {
            Log.e(TAG_ADS, "$adType -> startPreloading: Premium user")
            listener?.onResponse(false)
            return
        }

        if (adUnitId.trim().isEmpty()) {
            Log.e(TAG_ADS, "$adType -> startPreloading: Ad id is empty")
            listener?.onResponse(false)
            return
        }

        if (!internetManager.isInternetConnected) {
            Log.e(TAG_ADS, "$adType -> startPreloading: Internet is not connected")
            listener?.onResponse(false)
            return
        }

        if (bufferSize != null) {
            bufferSizeMap[adUnitId] = bufferSize
        }

        if (preloadStatusMap[adUnitId] == true) {
            Log.i(TAG_ADS, "$adType -> startPreloading: Preload already started for ad unit: $adUnitId")
            listener?.onResponse(isAdAvailable(adUnitId))
            return
        }

        if (isAdAvailable(adUnitId)) {
            Log.i(TAG_ADS, "$adType -> startPreloading: Ad already available")
            preloadStatusMap[adUnitId] = true
            listener?.onResponse(true)
            return
        }

        Log.d(TAG_ADS, "$adType -> startPreloading: Requesting admob server for ad...")

        val preloadCallback = object : PreloadCallback {
            override fun onAdPreloaded(preloadId: String, responseInfo: ResponseInfo) {
                Log.i(TAG_ADS, "$adType -> startPreloading: onAdPreloaded: preloadId: $preloadId")
                preloadStatusMap[adUnitId] = true
                postToMain { listener?.onResponse(true) }
            }

            override fun onAdFailedToPreload(preloadId: String, adError: LoadAdError) {
                Log.e(TAG_ADS, "$adType -> startPreloading: onAdFailedToPreload: preloadId: $preloadId, adMessage: ${adError.message}")
                preloadStatusMap[adUnitId] = false
                if (!bufferSizeMap.containsKey(adUnitId)) {
                    stopPreloading(adUnitId)
                }
                postToMain { listener?.onResponse(false) }
            }

            override fun onAdsExhausted(preloadId: String) {
                Log.d(TAG_ADS, "$adType -> startPreloading: onAdsExhausted: preloadId: $preloadId")
            }
        }

        try {
            val preloadConfig = createPreloadConfig(adUnitId, bufferSize)
            val isIdInUse = InterstitialAdPreloader.start(adUnitId, preloadConfig, preloadCallback)

            if (!isIdInUse) {
                Log.d(TAG_ADS, "$adType -> startPreloading: Preload ID is already in use")
                preloadStatusMap[adUnitId] = true
                listener?.onResponse(isAdAvailable(adUnitId))
            } else {
                preloadStatusMap[adUnitId] = true
            }
        } catch (e: Exception) {
            Log.e(TAG_ADS, "$adType -> startPreloading: Exception: ${e.message}", e)
            preloadStatusMap[adUnitId] = false
            listener?.onResponse(false)
        }
    }

    protected fun showPreloadedAd(
        activity: Activity?,
        adType: String,
        adUnitId: String,
        listener: InterstitialOnShowCallBack?
    ) {
        if (sharedPreferencesDataSource.isAppPurchased) {
            Log.e(TAG_ADS, "$adType -> showPreloadedAd: Premium user")
            listener?.onAdFailedToShow()
            return
        }

        if (!isAdAvailable(adUnitId)) {
            Log.e(TAG_ADS, "$adType -> showPreloadedAd: Interstitial is not available yet")
            listener?.onAdFailedToShow()
            return
        }

        if (activity == null) {
            Log.e(TAG_ADS, "$adType -> showPreloadedAd: activity reference is null")
            listener?.onAdFailedToShow()
            return
        }

        if (activity.isFinishing || activity.isDestroyed) {
            Log.e(TAG_ADS, "$adType -> showPreloadedAd: activity is finishing or destroyed")
            listener?.onAdFailedToShow()
            return
        }

        if (adUnitId.trim().isEmpty()) {
            Log.e(TAG_ADS, "$adType -> showPreloadedAd: Ad id is empty")
            listener?.onAdFailedToShow()
            return
        }

        Log.d(TAG_ADS, "$adType -> showPreloadedAd: showing ad")

        val ad: InterstitialAd? = pollAd(adUnitId)

        if (ad == null) {
            Log.e(TAG_ADS, "$adType -> showPreloadedAd: Failed to poll ad")
            listener?.onAdFailedToShow()
            return
        }

        ad.adEventCallback = object : InterstitialAdEventCallback {
            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                Log.d(TAG_ADS, "$adType -> showPreloadedAd: onAdShowedFullScreenContent: called")
                postToMain { listener?.onAdShowedFullScreenContent() }
            }

            override fun onAdImpression() {
                super.onAdImpression()
                Log.w(TAG_ADS, "$adType -> showPreloadedAd: onAdImpression: called")
                adShownMap[adUnitId] = true
                postToMain { listener?.onAdImpression() }
                postToMainDelayed { listener?.onAdImpressionDelayed() }
                if (!bufferSizeMap.containsKey(adUnitId)) {
                    stopPreloading(adUnitId)
                }
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                Log.d(TAG_ADS, "$adType -> showPreloadedAd: onAdDismissedFullScreenContent: called")
                postToMain { listener?.onAdDismissedFullScreenContent() }
            }

            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                super.onAdFailedToShowFullScreenContent(fullScreenContentError)
                Log.e(TAG_ADS, "$adType -> showPreloadedAd: onAdFailedToShowFullScreenContent: ${fullScreenContentError.code} -- ${fullScreenContentError.message}")
                if (!bufferSizeMap.containsKey(adUnitId)) {
                    stopPreloading(adUnitId)
                }
                postToMain { listener?.onAdFailedToShow() }
            }

            override fun onAdClicked() {
                super.onAdClicked()
                Log.d(TAG_ADS, "$adType -> showPreloadedAd: onAdClicked: called")
                postToMain { listener?.onAdClicked() }
            }

            override fun onAdPaid(value: AdValue) {
                super.onAdPaid(value)
                Log.d(TAG_ADS, "$adType -> showPreloadedAd: onAdPaid: ${value.valueMicros} ${value.currencyCode}")
            }
        }

        try {
            ad.show(activity)
        } catch (e: Exception) {
            Log.e(TAG_ADS, "$adType -> showPreloadedAd: Exception showing ad: ${e.message}", e)
            listener?.onAdFailedToShow()
        }
    }

    fun isInterstitialAvailable(adUnitId: String): Boolean {
        return isAdAvailable(adUnitId)
    }

    fun stopPreloading(adUnitId: String) {
        try {
            destroyPreload(adUnitId)
            preloadStatusMap.remove(adUnitId)
            bufferSizeMap.remove(adUnitId)
            adShownMap.remove(adUnitId)
            Log.d(TAG_ADS, "stopPreloading: Stopped preloading for ad unit: $adUnitId")
        } catch (e: Exception) {
            Log.e(TAG_ADS, "stopPreloading: Exception: ${e.message}", e)
        }
    }

    fun isPreloadActive(adUnitId: String): Boolean {
        return preloadStatusMap[adUnitId] == true
    }

    protected fun wasAdShown(adUnitId: String): Boolean {
        return adShownMap[adUnitId] == true
    }

    fun clearAll() {
        preloadStatusMap.clear()
        bufferSizeMap.clear()
        adShownMap.clear()
        mainHandler.removeCallbacksAndMessages(null)
    }
}