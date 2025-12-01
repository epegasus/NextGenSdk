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
    private val adShownMap = ConcurrentHashMap<String, Boolean>()
    private val mainHandler = Handler(Looper.getMainLooper())

    protected fun startPreloadingNative(
        adType: String,
        adUnitId: String,
        isRemoteEnable: Boolean,
        listener: NativeOnLoadCallback? = null
    ) {
        if (!isRemoteEnable) {
            Log.e(TAG_ADS, "$adType -> startPreloadingNative: Remote config is off")
            listener?.onResponse(false); return
        }
        if (sharedPrefs.isAppPurchased) {
            Log.e(TAG_ADS, "$adType -> startPreloadingNative: Premium user")
            listener?.onResponse(false); return
        }
        if (adUnitId.isBlank()) {
            Log.e(TAG_ADS, "$adType -> startPreloadingNative: Ad id is empty")
            listener?.onResponse(false); return
        }
        if (!internetManager.isInternetConnected) {
            Log.e(TAG_ADS, "$adType -> startPreloadingNative: Internet is not connected")
            listener?.onResponse(false); return
        }

        if (preloadStatusMap[adUnitId] == true &&
            NativeAdPreloader.isAdAvailable(adUnitId)
        ) {
            Log.d(TAG_ADS, "$adType -> startPreloadingNative: Ad already available")
            listener?.onResponse(true); return
        }

        val request = NativeAdRequest.Builder(
            adUnitId,
            listOf(NativeAd.NativeAdType.NATIVE)
        ).build()

        val preloadConfig = PreloadConfiguration(request)
        Log.d(TAG_ADS, "$adType -> startPreloadingNative: requesting server")

        val callback = object : PreloadCallback {
            override fun onAdPreloaded(preloadId: String, responseInfo: ResponseInfo) {
                Log.i(TAG_ADS, "$adType -> startPreloadingNative: onAdPreloaded: $preloadId")
                preloadStatusMap[adUnitId] = true
                postToMain { listener?.onResponse(true) }
            }

            override fun onAdFailedToPreload(preloadId: String, adError: LoadAdError) {
                Log.e(TAG_ADS, "$adType -> startPreloadingNative: failed: ${adError.message}")
                preloadStatusMap[adUnitId] = false
                postToMain { listener?.onResponse(false) }
            }

            override fun onAdsExhausted(preloadId: String) {
                Log.d(TAG_ADS, "$adType -> startPreloadingNative: onAdsExhausted: $preloadId")
            }
        }

        NativeAdPreloader.start(adUnitId, preloadConfig, callback)
        preloadStatusMap[adUnitId] = true
    }

    protected fun pollNativeAd(adType: String, adUnitId: String): NativeAd? {
        val result = NativeAdPreloader.pollAd(adUnitId)
        return if (result is NativeAdSuccess) {
            val ad = result.ad
            Log.d(TAG_ADS, "$adType -> pollNativeAd: got ad, responseInfo=${ad.getResponseInfo()}")
            ad
        } else {
            Log.e(TAG_ADS, "$adType -> pollNativeAd: no ad available")
            null
        }
    }

    protected fun attachShowCallbacks(
        adType: String,
        adUnitId: String,
        nativeAd: NativeAd,
        listener: NativeOnShowCallback?
    ) {
        nativeAd.adEventCallback = object : NativeAdEventCallback {
            override fun onAdImpression() {
                Log.v(TAG_ADS, "$adType -> showNative: onAdImpression")
                adShownMap[adUnitId] = true
                postToMain { listener?.onAdImpression() }
                postToMainDelayed { listener?.onAdImpressionDelayed() }
            }

            override fun onAdClicked() {
                Log.d(TAG_ADS, "$adType -> showNative: onAdClicked")
                postToMain { listener?.onAdImpression() }
            }

            override fun onAdPaid(value: AdValue) {
                Log.d(TAG_ADS, "$adType -> showNative: onPaid ${value.valueMicros} ${value.currencyCode}")
            }
        }
    }

    protected fun wasNativeShown(adUnitId: String): Boolean =
        adShownMap[adUnitId] == true

    fun clearAllNative() {
        preloadStatusMap.clear()
        adShownMap.clear()
        // NativeAdPreloader has no destroyAll() API in sample; we just clear our state.
    }

    private fun postToMain(action: () -> Unit) = mainHandler.post(action)

    private fun postToMainDelayed(delayMillis: Long = 300, action: () -> Unit) = mainHandler.postDelayed(action, delayMillis)
}