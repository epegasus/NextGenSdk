package com.hypersoft.admobpreloader.bannerAds

import android.content.res.Resources
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdPreloader
import com.hypersoft.admobpreloader.R
import com.hypersoft.admobpreloader.bannerAds.callbacks.BannerOnLoadCallback
import com.hypersoft.admobpreloader.bannerAds.callbacks.BannerOnShowCallback
import com.hypersoft.admobpreloader.bannerAds.engine.PreloadEngine
import com.hypersoft.admobpreloader.bannerAds.engine.ShowEngine
import com.hypersoft.admobpreloader.bannerAds.enums.BannerAdKey
import com.hypersoft.admobpreloader.bannerAds.model.AdConfig
import com.hypersoft.admobpreloader.bannerAds.model.AdInfo
import com.hypersoft.admobpreloader.bannerAds.storage.AdRegistry
import com.hypersoft.admobpreloader.utils.AdLogger
import com.hypersoft.core.network.InternetManager
import com.hypersoft.core.storage.SharedPreferencesDataSource

/**
 * Top-level manager for Banner Ads, mirroring NativeAdsManager / InterstitialAdsManager.
 *
 * Responsibilities:
 *  - Validate (premium, internet, remote flag, adUnit empty)
 *  - Map BannerAdKey -> AdConfig
 *  - (Optionally) enforce marketing policies: canShare / canReuse / single-shot vs buffer
 *  - Delegate to PreloadEngine / ShowEngine
 *
 * Public API:
 *  @see loadBannerAd(key, listener)
 *  @see pollBannerAd(key, showCallback)
 *  @see clearBannerAd(key)
 *  @see clearAllBannerAds()
 */
class BannerAdsManager internal constructor(
    private val resources: Resources,
    private val registry: AdRegistry,
    private val preloadEngine: PreloadEngine,
    private val showEngine: ShowEngine,
    private val internetManager: InternetManager,
    private val sharedPrefs: SharedPreferencesDataSource
) {

    private val adConfigMap: Map<BannerAdKey, AdConfig> by lazy {
        mapOf(
            BannerAdKey.ENTRANCE to AdConfig(
                adUnitId = resources.getString(R.string.admob_banner_entrance_id),
                isRemoteEnabled = sharedPrefs.rcBannerEntrance != 0,
                bufferSize = null,
                canShare = false,
                canReuse = false
            ),
            BannerAdKey.ON_BOARDING to AdConfig(
                adUnitId = resources.getString(R.string.admob_banner_on_boarding_id),
                isRemoteEnabled = sharedPrefs.rcBannerOnBoarding != 0,
                bufferSize = null,
                canShare = false,
                canReuse = false
            ),
            BannerAdKey.DASHBOARD to AdConfig(
                adUnitId = resources.getString(R.string.admob_banner_dashboard_id),
                isRemoteEnabled = sharedPrefs.rcBannerDashboard != 0,
                bufferSize = null,
                canShare = false,
                canReuse = false
            ),
            BannerAdKey.FEATURE to AdConfig(
                adUnitId = resources.getString(R.string.admob_banner_feature_id),
                isRemoteEnabled = sharedPrefs.rcBannerFeature != 0,
                bufferSize = null,
                canShare = false,
                canReuse = false
            ),
            BannerAdKey.EXIT to AdConfig(
                adUnitId = resources.getString(R.string.admob_banner_exit_id),
                isRemoteEnabled = sharedPrefs.rcBannerExit != 0,
                bufferSize = null,
                canShare = false,
                canReuse = false
            )
        )
    }

    /**
     * Preload a banner ad for the given placement key.
     */
    fun loadBannerAd(key: BannerAdKey, listener: BannerOnLoadCallback? = null) {
        val config = adConfigMap[key] ?: run {
            AdLogger.logError(key.value, "loadBannerAd", "Unknown key")
            listener?.onResponse(false)
            return
        }

        // Validations
        when {
            !config.isRemoteEnabled -> {
                AdLogger.logError(key.value, "loadBannerAd", "Remote config disabled")
                listener?.onResponse(false)
                return
            }

            sharedPrefs.isAppPurchased -> {
                AdLogger.logDebug(key.value, "loadBannerAd", "Premium user")
                listener?.onResponse(false)
                return
            }

            config.adUnitId.trim().isEmpty() -> {
                AdLogger.logError(key.value, "loadBannerAd", "AdUnit id empty")
                listener?.onResponse(false)
                return
            }

            !internetManager.isInternetConnected -> {
                AdLogger.logError(key.value, "loadBannerAd", "No internet")
                listener?.onResponse(false)
                return
            }
        }

        // register config for lookups
        registry.putInfo(key, AdInfo(config.adUnitId, config.canShare, config.canReuse, config.bufferSize))

        AdLogger.logDebug(key.value, "loadBannerAd", "Requesting server for banner ad...")
        preloadEngine.startPreload(
            key,
            AdInfo(config.adUnitId, config.canShare, config.canReuse, config.bufferSize),
            listener
        )
    }

    /**
     * Polls a preloaded banner ad for the given placement key.
     *
     * The caller is responsible for attaching the returned BannerAd view into a container.
     */
    fun pollBannerAd(
        key: BannerAdKey,
        showCallback: BannerOnShowCallback? = null
    ): BannerAd? {
        val info = registry.getInfo(key) ?: run {
            AdLogger.logError(key.value, "pollBannerAd", "Ad info not found for this key. Did you call loadBannerAd()?")
            showCallback?.onAdFailedToShow()
            return null
        }

        return showEngine.pollAd(key, info.adUnitId, showCallback)
    }

    /**
     * Clear a specific placement's banner ad and stop preloading if needed.
     */
    fun clearBannerAd(key: BannerAdKey) {
        val adUnitId = registry.getInfo(key)?.adUnitId ?: return
        AdLogger.logDebug(key.value, "clearBannerAd", "Clearing banner ad")
        preloadEngine.stopPreload(key, adUnitId)
        registry.removeInfo(key)
    }

    /**
     * Clear all banner placement state.
     */
    fun clearAllBannerAds() {
        AdLogger.logDebug("", "clearAllBannerAds", "Clearing all banner ads")
        registry.clearAll()
        preloadEngine.stopAll()
    }
}


