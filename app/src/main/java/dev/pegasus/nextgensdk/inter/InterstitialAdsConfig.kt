package dev.pegasus.nextgensdk.inter

import android.app.Activity
import android.content.res.Resources
import android.util.Log
import androidx.annotation.StringRes
import dev.pegasus.nextgensdk.R
import dev.pegasus.nextgensdk.inter.callbacks.InterstitialOnLoadCallBack
import dev.pegasus.nextgensdk.inter.callbacks.InterstitialOnShowCallBack
import dev.pegasus.nextgensdk.inter.enums.InterAdKey
import dev.pegasus.nextgensdk.inter.manager.InterstitialAdsManager
import dev.pegasus.nextgensdk.utils.Constants
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
 * Configuration class for Interstitial Ads using AdMob Next-Gen SDK Preload API
 * Extends InterstitialAdsManager and adds counter logic and ad unit mapping
 */

class InterstitialAdsConfig(
    private val resources: Resources,
    sharedPreferencesDataSource: SharedPreferencesDataSource,
    internetManager: InternetManager
) : InterstitialAdsManager(sharedPreferencesDataSource, internetManager) {

    // Counter map for managing ad load frequency (similar to old template)
    private val counterMap by lazy { HashMap<String, Int>() }

    // Map to store ad unit IDs for each ad key
    private val adUnitIdMap = mutableMapOf<String, String>()

    /**
     * Load interstitial ad with remote config validation
     * @param adType The ad key enum
     * @param listener Optional callback for load result
     */
    fun loadInterstitialAd(
        adType: InterAdKey,
        listener: InterstitialOnLoadCallBack? = null
    ) {
        var interAdId = ""
        var isRemoteEnable = false

        when (adType) {
            InterAdKey.ENTRANCE -> {
                interAdId = getResString(R.string.admob_inter_entrance_id)
                isRemoteEnable = sharedPreferencesDataSource.rcInterEntrance != 0
            }
            // Add more ad types here as needed
        }

        // Store ad unit ID for this ad type
        adUnitIdMap[adType.value] = interAdId

        // Start preloading with buffer size 1
        startPreloading(
            adType = adType.value,
            adUnitId = interAdId,
            isRemoteEnable = isRemoteEnable,
            bufferSize = 1,
            listener = listener
        )
    }

    /**
     * Load interstitial ad with counter logic (similar to old template)
     * @param adType The ad key enum
     * @param remoteCounter Pass remote counter value, if the value is n, it will load on "n-1". In case of <= 2, it will load everytime
     * @param loadOnStart Determine whether ad should be load on the very first time or not?
     * @param listener Optional callback for load result
     *
     * e.g. remoteCounter = 3, ad will load on "n-1" = 2
     *     if (loadOnStart) {
     *         // 1, 0, 0, 1, 0, 0, 1, 0, 0 ... so on
     *     } else {
     *         // 0, 0, 1, 0, 0, 1, 0, 0, 1 ... so on
     *     }
     */
    fun loadInterstitialAd(
        adType: InterAdKey,
        remoteCounter: Int,
        loadOnStart: Boolean,
        listener: InterstitialOnLoadCallBack? = null
    ) {
        when (loadOnStart) {
            true -> counterMap.putIfAbsent(adType.value, remoteCounter - 1)
            false -> counterMap.putIfAbsent(adType.value, 0)
        }

        if (counterMap.containsKey(adType.value)) {
            val counter = counterMap[adType.value] ?: 0
            counterMap[adType.value] = counter + 1
            counterMap[adType.value]?.let { currentCounter ->
                Log.d(Constants.TAG_ADS, "${adType.value} -> loadInterstitial_Counter ----- Total Counter: $remoteCounter, Current Counter: $currentCounter")
                if (currentCounter >= remoteCounter - 1) {
                    counterMap[adType.value] = 0
                    loadInterstitialAd(adType = adType, listener = listener)
                    return
                }
            }
        }

        // Counter not reached
        listener?.onResponse(false)
    }

    /**
     * Show interstitial ad with all validations
     * @param activity The activity to show the ad
     * @param adType The ad key enum
     * @param listener Optional callback for show events
     */
    fun showInterstitialAd(
        activity: Activity?,
        adType: InterAdKey,
        listener: InterstitialOnShowCallBack? = null
    ) {
        val adUnitId = adUnitIdMap[adType.value] ?: run {
            Log.e(Constants.TAG_ADS, "${adType.value} -> showInterstitialAd: Ad unit ID not found. Make sure to load ad first.")
            listener?.onAdFailedToShow()
            return
        }

        showPreloadedAd(
            activity = activity,
            adType = adType.value,
            adUnitId = adUnitId,
            listener = listener
        )
    }

    /**
     * Check if interstitial ad is loaded/available
     * @param adType The ad key enum
     * @return true if ad is available, false otherwise
     */
    fun isInterstitialAdLoaded(adType: InterAdKey): Boolean {
        val adUnitId = adUnitIdMap[adType.value] ?: return false
        return isInterstitialAvailable(adUnitId)
    }

    /**
     * Stop preloading for a specific ad type
     * @param adType The ad key enum
     */
    fun stopPreloading(adType: InterAdKey) {
        val adUnitId = adUnitIdMap[adType.value] ?: return
        stopPreloading(adUnitId)
    }

    /**
     * Get resource string by ID
     */
    private fun getResString(@StringRes resId: Int): String {
        return resources.getString(resId)
    }
}