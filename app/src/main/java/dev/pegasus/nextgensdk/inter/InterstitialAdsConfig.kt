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

    // Primary maps using InterAdKey as key
    private val adInfoMap = ConcurrentHashMap<InterAdKey, AdInfo>()
    private val counterMap = ConcurrentHashMap<InterAdKey, Int>()

    // Configuration data - maps InterAdKey to its config
    private val adConfigMap = mapOf(
        InterAdKey.ENTRANCE to AdConfigData(
            resId = R.string.admob_inter_entrance_id,
            remoteConfigKey = { sharedPreferencesDataSource.rcInterEntrance != 0 },
            bufferSize = null,
            canShare = true,
            canReuse = true
        ),
        InterAdKey.ON_BOARDING to AdConfigData(
            resId = R.string.admob_inter_on_boarding_id,
            remoteConfigKey = { sharedPreferencesDataSource.rcInterOnBoarding != 0 },
            bufferSize = null,
            canShare = true,
            canReuse = true
        ),
        InterAdKey.DASHBOARD to AdConfigData(
            resId = R.string.admob_inter_dashboard_id,
            remoteConfigKey = { sharedPreferencesDataSource.rcInterDashboard != 0 },
            bufferSize = 1,
            canShare = false,
            canReuse = true
        ),
        InterAdKey.BOTTOM_NAVIGATION to AdConfigData(
            resId = R.string.admob_inter_bottom_navigation_id,
            remoteConfigKey = { sharedPreferencesDataSource.rcInterBottomNavigation != 0 },
            bufferSize = 1,
            canShare = false,
            canReuse = true
        ),
        InterAdKey.BACK_PRESS to AdConfigData(
            resId = R.string.admob_inter_back_press_id,
            remoteConfigKey = { sharedPreferencesDataSource.rcInterBackpress != 0 },
            bufferSize = 1,
            canShare = true,
            canReuse = true
        ),
        InterAdKey.EXIT to AdConfigData(
            resId = R.string.admob_inter_exit_id,
            remoteConfigKey = { sharedPreferencesDataSource.rcInterExit != 0 },
            bufferSize = null,
            canShare = false,
            canReuse = true
        )
    )

    private fun getAdConfig(adType: InterAdKey): AdConfig? {
        val configData = adConfigMap[adType] ?: return null
        return AdConfig(
            adUnitId = getResString(configData.resId),
            isRemoteEnable = configData.remoteConfigKey(),
            bufferSize = configData.bufferSize,
            canShare = configData.canShare,
            canReuse = configData.canReuse
        )
    }

    fun loadInterstitialAd(
        adType: InterAdKey,
        listener: InterstitialOnLoadCallBack? = null
    ) {
        val config = getAdConfig(adType) ?: run {
            logError(adType.value, "loadInterstitialAd", "Unknown ad type")
            listener?.onResponse(false)
            return
        }

        // Try to reuse existing ad if allowed
        if (config.canReuse) {
            findReusableAd(adType, config.adUnitId)?.let { reusableAdType ->
                adInfoMap[reusableAdType]?.let { reusableInfo ->
                    logDebug(adType.value, "loadInterstitialAd", "Reusing available ad from ${reusableAdType.value}")
                    adInfoMap[adType] = reusableInfo.copy(canShare = config.canShare, canReuse = config.canReuse, bufferSize = config.bufferSize)
                    listener?.onResponse(true)
                    return
                }
            }
        }

        // Stop existing preload if bufferSize=null and not shown
        if (config.bufferSize == null && isPreloadActive(config.adUnitId)) {
            findAdTypeByAdUnitId(config.adUnitId)?.takeIf { !wasAdShown(config.adUnitId) }?.let {
                logDebug(adType.value, "loadInterstitialAd", "Stopping existing preload for ${it.value} (bufferSize=null, not shown)")
                stopPreloading(it)
            }
        }

        // Load new ad
        loadInterstitialAd(
            adType = adType.value,
            adUnitId = config.adUnitId,
            isRemoteEnable = config.isRemoteEnable,
            bufferSize = config.bufferSize,
            listener = createLoadListener(adType, config, listener)
        )
    }

    fun loadInterstitialAd(
        adType: InterAdKey,
        remoteCounter: Int,
        loadOnStart: Boolean,
        listener: InterstitialOnLoadCallBack? = null
    ) {
        counterMap.putIfAbsent(adType, if (loadOnStart) remoteCounter - 1 else 0)
        val currentCounter = counterMap[adType]!! + 1
        counterMap[adType] = currentCounter

        Log.d(TAG_ADS, "${adType.value} -> loadInterstitial_Counter ----- Total Counter: $remoteCounter, Current Counter: $currentCounter")

        if (currentCounter >= remoteCounter) {
            counterMap[adType] = 0
            loadInterstitialAd(adType, listener)
        } else {
            listener?.onResponse(false)
        }
    }

    fun showInterstitialAd(activity: Activity?, adType: InterAdKey, listener: InterstitialOnShowCallBack? = null) {
        val config = getAdConfig(adType) ?: run {
            logError(adType.value, "showInterstitialAd", "Unknown ad type")
            listener?.onAdFailedToShow()
            return
        }

        val adInfo = adInfoMap[adType]

        // Try to reuse if allowed
        if (config.canReuse) {
            findReusableAd(adType, adInfo?.adUnitId)?.let { reusableAdType ->
                adInfoMap[reusableAdType]?.let { reusableInfo ->
                    logDebug(adType.value, "showInterstitialAd", "Reusing available ad from ${reusableAdType.value}")
                    showInterstitialAd(activity, reusableAdType.value, reusableInfo.adUnitId, listener)
                    return
                }
            }
        }

        // Validate and show own ad
        when {
            !config.canReuse && adInfo == null -> {
                logError(adType.value, "showInterstitialAd", "Ad not loaded for this screen. canReuse=false, so cannot use other ads.")
                listener?.onAdFailedToShow()
            }
            adInfo == null -> {
                logError(adType.value, "showInterstitialAd", "Ad unit ID not found. Make sure to load ad first.")
                listener?.onAdFailedToShow()
            }
            else -> showInterstitialAd(activity, adType.value, adInfo.adUnitId, listener)
        }
    }

    private fun findAdTypeByAdUnitId(adUnitId: String): InterAdKey? {
        return adInfoMap.entries.firstOrNull { it.value.adUnitId == adUnitId }?.key
    }

    private fun findReusableAd(requestedAdType: InterAdKey, requestedAdUnitId: String?): InterAdKey? {
        // Helper to check if ad is reusable
        fun isReusable(adType: InterAdKey, info: AdInfo): Boolean {
            return adType != requestedAdType
                && !wasAdShown(info.adUnitId)
                && isInterstitialAvailable(info.adUnitId)
                && isPreloadActive(info.adUnitId)
                && info.canShare
        }

        // Priority: same adUnitId match
        requestedAdUnitId?.let { targetId ->
            adInfoMap.entries.firstOrNull { (type, info) ->
                type != requestedAdType && info.adUnitId == targetId && isReusable(type, info)
            }?.let {
                logDebug(requestedAdType.value, "findReusableAd", "Found same ad unit ID from ${it.key.value}")
                return it.key
            }
        }

        // Fallback: any shareable ad
        return adInfoMap.entries.firstOrNull { (type, info) -> isReusable(type, info) }?.key
    }

    fun isInterstitialAdLoaded(adType: InterAdKey): Boolean {
        val config = getAdConfig(adType) ?: return false
        val adInfo = adInfoMap[adType]

        return if (config.canReuse) {
            findReusableAd(adType, adInfo?.adUnitId) != null
        } else {
            adInfo != null && isInterstitialAvailable(adInfo.adUnitId)
        }
    }

    fun stopPreloading(adType: InterAdKey) {
        adInfoMap[adType]?.adUnitId?.let { stopPreloading(it) }
    }

    override fun stopPreloading(adUnitId: String) {
        // Find and remove all adTypes using this adUnitId
        adInfoMap.keys.filter { adInfoMap[it]?.adUnitId == adUnitId }.forEach {
            adInfoMap.remove(it)
            counterMap.remove(it)
        }
        super.stopPreloading(adUnitId)
    }

    fun clearAdData(adType: InterAdKey) {
        adInfoMap.remove(adType)?.adUnitId?.let { stopPreloading(it) }
        counterMap.remove(adType)
    }

    // Helper functions
    private fun getResString(@StringRes resId: Int) = resources.getString(resId)
    
    private fun logError(adType: String, method: String, message: String) {
        Log.e(TAG_ADS, "$adType -> $method: $message")
    }
    
    private fun logDebug(adType: String, method: String, message: String) {
        Log.d(TAG_ADS, "$adType -> $method: $message")
    }

    private fun createLoadListener(
        adType: InterAdKey,
        config: AdConfig,
        originalListener: InterstitialOnLoadCallBack?
    ) = object : InterstitialOnLoadCallBack {
        override fun onResponse(successfullyLoaded: Boolean) {
            if (successfullyLoaded) {
                adInfoMap[adType] = AdInfo(
                    adUnitId = config.adUnitId,
                    canShare = config.canShare,
                    canReuse = config.canReuse,
                    bufferSize = config.bufferSize
                )
            }
            originalListener?.onResponse(successfullyLoaded)
        }
    }

    // Data classes
    private data class AdConfigData(
        @StringRes val resId: Int,
        val remoteConfigKey: () -> Boolean,
        val bufferSize: Int?,
        val canShare: Boolean,
        val canReuse: Boolean
    )

    private data class AdConfig(
        val adUnitId: String,
        val isRemoteEnable: Boolean,
        val bufferSize: Int?,
        val canShare: Boolean,
        val canReuse: Boolean
    )

    private data class AdInfo(
        val adUnitId: String,
        val canShare: Boolean,
        val canReuse: Boolean,
        val bufferSize: Int?
    )
}