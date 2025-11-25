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
import dev.pegasus.nextgensdk.utils.Constants.TAG_ADS
import dev.pegasus.nextgensdk.utils.InternetManager
import dev.pegasus.nextgensdk.utils.SharedPreferencesDataSource

/**
 * Created by: Sohaib Ahmed
 * Date: 1/16/2025
 *
 * Links:
 * - LinkedIn: https://linkedin.com/in/epegasus
 * - GitHub: https://github.com/epegasus
 *
 * Manager class for handling Interstitial Ads using AdMob Next-Gen SDK Preload API
 * Includes all validations from old template and optimizations for performance and show rate
 */

abstract class InterstitialAdsManager(
    protected val sharedPreferencesDataSource: SharedPreferencesDataSource,
    protected val internetManager: InternetManager
) {

    // Track preload status for each ad unit to prevent duplicate preloading
    private val preloadStatusMap = mutableMapOf<String, Boolean>()

    /**
     * Create PreloadConfiguration with buffer size
     */
    private fun createPreloadConfig(adUnitId: String, bufferSize: Int = 1): PreloadConfiguration {
        val adRequest = AdRequest.Builder(adUnitId).build()
        return PreloadConfiguration(adRequest, bufferSize)
    }

    /**
     * Check if ad is available
     */
    private fun isAdAvailable(adUnitId: String): Boolean {
        return InterstitialAdPreloader.isAdAvailable(adUnitId)
    }

    /**
     * Poll ad (returns next available ad and loads another in background)
     */
    private fun pollAd(adUnitId: String): InterstitialAd? {
        return InterstitialAdPreloader.pollAd(adUnitId)
    }

    /**
     * Stop preloading for ad unit
     */
    private fun destroyPreload(adUnitId: String) {
        InterstitialAdPreloader.destroy(adUnitId)
    }

    /**
     * Start preloading interstitial ads with all validations
     * Uses callbacks for async handling
     */
    protected fun startPreloading(
        adType: String,
        adUnitId: String,
        isRemoteEnable: Boolean,
        bufferSize: Int = 1,
        listener: InterstitialOnLoadCallBack? = null
    ) {
        // Validation: Check if remote config is enabled
        if (!isRemoteEnable) {
            Log.e(TAG_ADS, "$adType -> startPreloading: Remote config is off")
            listener?.onResponse(false)
            return
        }

        // Validation: Check if premium user
        if (sharedPreferencesDataSource.isAppPurchased) {
            Log.e(TAG_ADS, "$adType -> startPreloading: Premium user")
            listener?.onResponse(false)
            return
        }

        // Validation: Check if ad unit ID is empty
        if (adUnitId.trim().isEmpty()) {
            Log.e(TAG_ADS, "$adType -> startPreloading: Ad id is empty")
            listener?.onResponse(false)
            return
        }

        // Validation: Check if internet is connected
        if (!internetManager.isInternetConnected) {
            Log.e(TAG_ADS, "$adType -> startPreloading: Internet is not connected")
            listener?.onResponse(false)
            return
        }

        // Validation: Check if preload is already started for this ad unit
        if (preloadStatusMap[adUnitId] == true) {
            Log.i(TAG_ADS, "$adType -> startPreloading: Preload already started for ad unit: $adUnitId")
            // Check if ad is available
            val isAvailable = isAdAvailable(adUnitId)
            listener?.onResponse(isAvailable)
            return
        }

        // Check if ad is already available (preload might have been started elsewhere)
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
                listener?.onResponse(true)
            }

            override fun onAdFailedToPreload(preloadId: String, adError: LoadAdError) {
                Log.e(TAG_ADS, "$adType -> startPreloading: onAdFailedToPreload: preloadId: $preloadId, adMessage: ${adError.message}")
                preloadStatusMap[adUnitId] = false
                listener?.onResponse(false)
            }

            override fun onAdsExhausted(preloadId: String) {
                Log.d(TAG_ADS, "$adType -> startPreloading: onAdsExhausted: preloadId: $preloadId")
            }
        }

        try {
            val preloadConfig = createPreloadConfig(adUnitId, bufferSize)
            val isIdInUse = InterstitialAdPreloader.start(adUnitId, preloadConfig, preloadCallback)

            if (!isIdInUse) {
                // Preload ID is already in use, meaning preload was already started
                Log.d(TAG_ADS, "$adType -> startPreloading: Preload ID is already in use")
                preloadStatusMap[adUnitId] = true
                val isAvailable = isAdAvailable(adUnitId)
                listener?.onResponse(isAvailable)
            } else {
                // Preload started successfully, wait for callback
                preloadStatusMap[adUnitId] = true
            }
        } catch (e: Exception) {
            Log.e(TAG_ADS, "$adType -> startPreloading: Exception: ${e.message}", e)
            preloadStatusMap[adUnitId] = false
            listener?.onResponse(false)
        }
    }

    /**
     * Show preloaded interstitial ad with all validations
     */
    protected fun showPreloadedAd(
        activity: Activity?,
        adType: String,
        adUnitId: String,
        listener: InterstitialOnShowCallBack?
    ) {

        // Validation: Check if premium user
        if (sharedPreferencesDataSource.isAppPurchased) {
            Log.e(TAG_ADS, "$adType -> showPreloadedAd: Premium user")
            listener?.onAdFailedToShow()
            return
        }

        // Validation: Check if ad is available
        if (!isAdAvailable(adUnitId)) {
            Log.e(TAG_ADS, "$adType -> showPreloadedAd: Interstitial is not available yet")
            listener?.onAdFailedToShow()
            return
        }

        // Validation: Check if activity is null
        if (activity == null) {
            Log.e(TAG_ADS, "$adType -> showPreloadedAd: activity reference is null")
            listener?.onAdFailedToShow()
            return
        }

        // Validation: Check if activity is finishing or destroyed
        if (activity.isFinishing || activity.isDestroyed) {
            Log.e(TAG_ADS, "$adType -> showPreloadedAd: activity is finishing or destroyed")
            listener?.onAdFailedToShow()
            return
        }

        // Validation: Check if ad unit ID is empty
        if (adUnitId.trim().isEmpty()) {
            Log.e(TAG_ADS, "$adType -> showPreloadedAd: Ad id is empty")
            listener?.onAdFailedToShow()
            return
        }

        Log.d(TAG_ADS, "$adType -> showPreloadedAd: showing ad")

        // Polling returns the next available ad and loads another ad in the background
        val ad: InterstitialAd? = pollAd(adUnitId)

        if (ad == null) {
            Log.e(TAG_ADS, "$adType -> showPreloadedAd: Failed to poll ad")
            listener?.onAdFailedToShow()
            return
        }

        // Set up ad event callbacks
        ad.adEventCallback = object : InterstitialAdEventCallback {
            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                Log.d(TAG_ADS, "$adType -> showPreloadedAd: onAdShowedFullScreenContent: called")
                listener?.onAdShowedFullScreenContent()
            }

            override fun onAdImpression() {
                super.onAdImpression()
                Log.d(TAG_ADS, "$adType -> showPreloadedAd: onAdImpression: called")
                listener?.onAdImpression()
                // Delayed callback for impression (similar to old template)
                Handler(Looper.getMainLooper()).postDelayed({ listener?.onAdImpressionDelayed() }, 300)
                destroyPreload(adUnitId)
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                Log.d(TAG_ADS, "$adType -> showPreloadedAd: onAdDismissedFullScreenContent: called")
                listener?.onAdDismissedFullScreenContent()
            }

            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                super.onAdFailedToShowFullScreenContent(fullScreenContentError)
                Log.e(TAG_ADS, "$adType -> showPreloadedAd: onAdFailedToShowFullScreenContent: ${fullScreenContentError.code} -- ${fullScreenContentError.message}")
                listener?.onAdFailedToShow()
            }

            override fun onAdClicked() {
                super.onAdClicked()
                Log.d(TAG_ADS, "$adType -> showPreloadedAd: onAdClicked: called")
                listener?.onAdClicked()
            }

            override fun onAdPaid(value: AdValue) {
                super.onAdPaid(value)
                Log.d(TAG_ADS, "$adType -> showPreloadedAd: onAdPaid: ${value.valueMicros} ${value.currencyCode}")
                // Revenue tracking can be added here if needed
            }
        }

        // Show the ad
        try {
            ad.show(activity)
        } catch (e: Exception) {
            Log.e(TAG_ADS, "$adType -> showPreloadedAd: Exception showing ad: ${e.message}", e)
            listener?.onAdFailedToShow()
        }
    }

    /**
     * Check if interstitial ad is available
     */
    fun isInterstitialAvailable(adUnitId: String): Boolean {
        return isAdAvailable(adUnitId)
    }

    /**
     * Stop preloading for a specific ad unit
     */
    fun stopPreloading(adUnitId: String) {
        try {
            destroyPreload(adUnitId)
            preloadStatusMap[adUnitId] = false
            Log.d(TAG_ADS, "stopPreloading: Stopped preloading for ad unit: $adUnitId")
        } catch (e: Exception) {
            Log.e(TAG_ADS, "stopPreloading: Exception: ${e.message}", e)
        }
    }

    /**
     * Check if preload is active for a specific ad unit
     */
    fun isPreloadActive(adUnitId: String): Boolean {
        return preloadStatusMap[adUnitId] == true
    }
}

