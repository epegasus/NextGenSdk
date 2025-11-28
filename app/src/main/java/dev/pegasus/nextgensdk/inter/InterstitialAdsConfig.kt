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
import dev.pegasus.nextgensdk.utils.constants.Constants.TAG_ADS
import dev.pegasus.nextgensdk.utils.network.InternetManager
import dev.pegasus.nextgensdk.utils.storage.SharedPreferencesDataSource
import java.util.concurrent.ConcurrentHashMap

class InterstitialAdsConfig(
    private val resources: Resources,
    sharedPreferencesDataSource: SharedPreferencesDataSource,
    internetManager: InternetManager
) : InterstitialAdsManager(sharedPreferencesDataSource, internetManager) {

    private val counterMap = ConcurrentHashMap<String, Int>()
    private val adUnitIdMap = ConcurrentHashMap<String, String>()
    private val canReuseMap = ConcurrentHashMap<String, Boolean>()
    private val canShareMap = ConcurrentHashMap<String, Boolean>()

    private fun getAdConfig(adType: InterAdKey): AdConfig? {
        return when (adType) {
            InterAdKey.ENTRANCE -> AdConfig(
                adUnitId = getResString(R.string.admob_inter_entrance_id),
                isRemoteEnable = sharedPreferencesDataSource.rcInterEntrance != 0,
                bufferSize = null,
                canShare = true,
                canReuse = true
            )

            InterAdKey.ON_BOARDING -> AdConfig(
                adUnitId = getResString(R.string.admob_inter_on_boarding_id),
                isRemoteEnable = sharedPreferencesDataSource.rcInterOnBoarding != 0,
                bufferSize = null,
                canShare = true,
                canReuse = true
            )

            InterAdKey.DASHBOARD -> AdConfig(
                adUnitId = getResString(R.string.admob_inter_dashboard_id),
                isRemoteEnable = sharedPreferencesDataSource.rcInterDashboard != 0,
                bufferSize = 1,
                canShare = false,
                canReuse = true
            )

            InterAdKey.BOTTOM_NAVIGATION -> AdConfig(
                adUnitId = getResString(R.string.admob_inter_bottom_navigation_id),
                isRemoteEnable = sharedPreferencesDataSource.rcInterBottomNavigation != 0,
                bufferSize = 1,
                canShare = false,
                canReuse = true
            )

            InterAdKey.BACK_PRESS -> AdConfig(
                adUnitId = getResString(R.string.admob_inter_back_press_id),
                isRemoteEnable = sharedPreferencesDataSource.rcInterBackpress != 0,
                bufferSize = 1,
                canShare = true,
                canReuse = true
            )

            InterAdKey.EXIT -> AdConfig(
                adUnitId = getResString(R.string.admob_inter_exit_id),
                isRemoteEnable = sharedPreferencesDataSource.rcInterExit != 0,
                bufferSize = null,
                canShare = false,
                canReuse = true
            )
        }
    }

    fun loadInterstitialAd(
        adType: InterAdKey,
        listener: InterstitialOnLoadCallBack? = null
    ) {
        val config = getAdConfig(adType) ?: run {
            Log.e(TAG_ADS, "${adType.value} -> loadInterstitialAd: Unknown ad type")
            listener?.onResponse(false)
            return
        }

        // If this screen can reuse others' ads, check for available reusable ads
        if (config.canReuse) {
            val reusableAd = findReusableAd(adType, config.adUnitId)
            if (reusableAd != null) {
                Log.d(TAG_ADS, "${adType.value} -> loadInterstitialAd: Reusable ad available from ${reusableAd.first}, skipping load")
                adUnitIdMap[adType.value] = reusableAd.second
                canReuseMap[adType.value] = config.canReuse
                canShareMap[adType.value] = config.canShare
                listener?.onResponse(true)
                return
            }
        }

        // Only add to map when ad successfully loads (use wrapper listener)
        startPreloading(
            adType = adType.value,
            adUnitId = config.adUnitId,
            isRemoteEnable = config.isRemoteEnable,
            bufferSize = config.bufferSize,
            listener = object : InterstitialOnLoadCallBack {
                override fun onResponse(successfullyLoaded: Boolean) {
                    if (successfullyLoaded) {
                        // Only add to map on successful load
                        adUnitIdMap[adType.value] = config.adUnitId
                        canReuseMap[adType.value] = config.canReuse
                        canShareMap[adType.value] = config.canShare
                    }
                    listener?.onResponse(successfullyLoaded)
                }
            }
        )
    }

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

        val currentCounter = counterMap[adType.value] ?: 0
        counterMap[adType.value] = currentCounter + 1

        Log.d(TAG_ADS, "${adType.value} -> loadInterstitial_Counter ----- Total Counter: $remoteCounter, Current Counter: ${currentCounter + 1}")

        if (currentCounter + 1 >= remoteCounter) {
            counterMap[adType.value] = 0
            loadInterstitialAd(adType = adType, listener = listener)
            return
        }

        listener?.onResponse(false)
    }

    fun showInterstitialAd(activity: Activity?, adType: InterAdKey, listener: InterstitialOnShowCallBack? = null) {
        val config = getAdConfig(adType) ?: run {
            Log.e(TAG_ADS, "${adType.value} -> showInterstitialAd: Unknown ad type")
            listener?.onAdFailedToShow()
            return
        }
        
        val canReuse = config.canReuse
        val requestedAdUnitId = adUnitIdMap[adType.value]

        // If this screen can reuse others' ads, check for available reusable ads
        if (canReuse) {
            val reusableAd = findReusableAd(adType, requestedAdUnitId)
            if (reusableAd != null) {
                Log.d(TAG_ADS, "${adType.value} -> showInterstitialAd: Reusing available ad from ${reusableAd.first}")
                showPreloadedAd(
                    activity = activity,
                    adType = reusableAd.first,
                    adUnitId = reusableAd.second,
                    listener = listener
                )
                return
            }
        }

        // When canReuse = false, we must ensure:
        // 1. Ad was loaded for THIS specific adType (check if adType exists in map)
        // 2. Validate based on InterAdKey, not adUnitId (works in debug with same test IDs)
        if (!canReuse) {
            // Check if ad was loaded for this specific adType
            if (!adUnitIdMap.containsKey(adType.value)) {
                Log.e(TAG_ADS, "${adType.value} -> showInterstitialAd: Ad not loaded for this screen. canReuse=false, so cannot use other ads.")
                listener?.onAdFailedToShow()
                return
            }
            
            // Additional validation: Ensure the adUnitId in map corresponds to this adType's expected adUnitId
            // This works even in debug mode because we're checking the map key (adType), not just adUnitId
            val adUnitId = adUnitIdMap[adType.value]
            if (adUnitId == null) {
                Log.e(TAG_ADS, "${adType.value} -> showInterstitialAd: Ad unit ID not found in map for this screen.")
                listener?.onAdFailedToShow()
                return
            }
            
            // Show the ad that was loaded for this specific screen
            showPreloadedAd(
                activity = activity,
                adType = adType.value,
                adUnitId = adUnitId,
                listener = listener
            )
            return
        }

        // When canReuse = true but no reusable ad found, use own ad if available
        val adUnitId = requestedAdUnitId ?: run {
            Log.e(TAG_ADS, "${adType.value} -> showInterstitialAd: Ad unit ID not found. Make sure to load ad first.")
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

    private fun findReusableAd(requestedAdType: InterAdKey, requestedAdUnitId: String? = null): Pair<String, String>? {
        // First, try to find ad with same adUnitId (priority match)
        if (requestedAdUnitId != null) {
            for ((adTypeValue, adUnitId) in adUnitIdMap) {
                if (adTypeValue == requestedAdType.value) continue
                if (adUnitId == requestedAdUnitId && isInterstitialAvailable(adUnitId) && !wasAdShown(adUnitId)) {
                    // Check if the source ad type allows sharing (canShare = true)
                    if (canShareMap[adTypeValue] == true) {
                        Log.d(TAG_ADS, "${requestedAdType.value} -> findReusableAd: Found same ad unit ID from $adTypeValue")
                        return Pair(adTypeValue, adUnitId)
                    }
                }
            }
        }

        // Then, find any available ad that can be shared
        for ((adTypeValue, adUnitId) in adUnitIdMap) {
            if (adTypeValue == requestedAdType.value) continue
            if (wasAdShown(adUnitId)) continue
            if (isInterstitialAvailable(adUnitId)) {
                // Check if the source ad type allows sharing (canShare = true)
                if (canShareMap[adTypeValue] == true) {
                    return Pair(adTypeValue, adUnitId)
                }
            }
        }

        return null
    }

    fun isInterstitialAdLoaded(adType: InterAdKey): Boolean {
        val config = getAdConfig(adType) ?: return false
        val canReuse = config.canReuse
        val requestedAdUnitId = adUnitIdMap[adType.value]

        // If this screen can reuse others' ads, check for available reusable ads
        if (canReuse) {
            val reusableAd = findReusableAd(adType, requestedAdUnitId)
            if (reusableAd != null) {
                return true
            }
        }

        val adUnitId = requestedAdUnitId ?: return false
        return isInterstitialAvailable(adUnitId)
    }

    fun stopPreloading(adType: InterAdKey) {
        val adUnitId = adUnitIdMap[adType.value] ?: return
        stopPreloading(adUnitId)
    }

    fun clearAdData(adType: InterAdKey) {
        val adUnitId = adUnitIdMap.remove(adType.value)
        canReuseMap.remove(adType.value)
        canShareMap.remove(adType.value)
        counterMap.remove(adType.value)
        adUnitId?.let { stopPreloading(it) }
    }

    private fun getResString(@StringRes resId: Int): String {
        return resources.getString(resId)
    }

    private data class AdConfig(
        val adUnitId: String,
        val isRemoteEnable: Boolean,
        val bufferSize: Int?,
        val canShare: Boolean,  // Can this ad be shared with other screens? (outgoing)
        val canReuse: Boolean   // Can this screen reuse ads from other screens? (incoming)
    )
}