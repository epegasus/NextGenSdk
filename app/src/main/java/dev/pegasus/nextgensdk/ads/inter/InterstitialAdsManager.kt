package dev.pegasus.nextgensdk.ads.inter

import android.app.Activity
import android.content.res.Resources
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdPreloader
import dev.pegasus.nextgensdk.R
import dev.pegasus.nextgensdk.ads.inter.callbacks.InterstitialLoadListener
import dev.pegasus.nextgensdk.ads.inter.callbacks.InterstitialShowListener
import dev.pegasus.nextgensdk.ads.inter.engine.PreloadEngine
import dev.pegasus.nextgensdk.ads.inter.engine.ShowEngine
import dev.pegasus.nextgensdk.ads.inter.enums.InterAdKey
import dev.pegasus.nextgensdk.ads.inter.model.AdConfig
import dev.pegasus.nextgensdk.ads.inter.model.AdInfo
import dev.pegasus.nextgensdk.ads.inter.storage.AdRegistry
import dev.pegasus.nextgensdk.utils.network.InternetManager
import dev.pegasus.nextgensdk.utils.storage.SharedPreferencesDataSource

/**
 * The top-level manager you should use in Fragments / Activities.
 * Responsibilities:
 * - Validate (premium, internet, remote flag, adUnit empty)
 * - Map InterAdKey -> AdConfig
 * - Enforce marketing policies: canShare / canReuse / single-shot vs buffer
 * - Delegate to PreloadEngine / ShowEngine
 *
 * Public API:
 *  - loadAd(key, listener)
 *  - showAd(activity, key, listener)
 *  - destroyAd(key)
 *  - destroyAllAds()
 */
class InterstitialAdsManager internal constructor(
    private val resources: Resources,
    private val registry: AdRegistry,
    private val preloadEngine: PreloadEngine,
    private val showEngine: ShowEngine,
    private val internetManager: InternetManager,
    private val sharedPrefs: SharedPreferencesDataSource
) {

    private val adConfigMap: Map<InterAdKey, AdConfig> by lazy {
        mapOf(
            InterAdKey.ENTRANCE to AdConfig(
                adUnitId = resources.getString(R.string.admob_inter_entrance_id),
                isRemoteEnabled = sharedPrefs.rcInterEntrance != 0,
                bufferSize = null,
                canShare = false,
                canReuse = false
            ),
            InterAdKey.ON_BOARDING to AdConfig(
                adUnitId = resources.getString(R.string.admob_inter_on_boarding_id),
                isRemoteEnabled = sharedPrefs.rcInterOnBoarding != 0,
                bufferSize = null,
                canShare = false,
                canReuse = false
            ),
            InterAdKey.DASHBOARD to AdConfig(
                adUnitId = resources.getString(R.string.admob_inter_dashboard_id),
                isRemoteEnabled = sharedPrefs.rcInterDashboard != 0,
                bufferSize = 1,
                canShare = false,
                canReuse = false
            ),
            InterAdKey.BOTTOM_NAVIGATION to AdConfig(
                adUnitId = resources.getString(R.string.admob_inter_bottom_navigation_id),
                isRemoteEnabled = sharedPrefs.rcInterBottomNavigation != 0,
                bufferSize = 1,
                canShare = false,
                canReuse = false
            ),
            InterAdKey.BACK_PRESS to AdConfig(
                adUnitId = resources.getString(R.string.admob_inter_back_press_id),
                isRemoteEnabled = sharedPrefs.rcInterBackpress != 0,
                bufferSize = 1,
                canShare = false,
                canReuse = false
            ),
            InterAdKey.EXIT to AdConfig(
                adUnitId = resources.getString(R.string.admob_inter_exit_id),
                isRemoteEnabled = sharedPrefs.rcInterExit != 0,
                bufferSize = null,
                canShare = false,
                canReuse = false
            )
        )
    }

    // Public API - minimal & friendly
    fun loadAd(key: InterAdKey, listener: InterstitialLoadListener? = null) {
        val config = adConfigMap[key] ?: run {
            listener?.onFailed(key.value, "Unknown key")
            return
        }

        // Validations
        when {
            !config.isRemoteEnabled -> {
                listener?.onFailed(key.value, "Remote config disabled")
                return
            }

            sharedPrefs.isAppPurchased -> {
                listener?.onFailed(key.value, "Premium user")
                return
            }

            config.adUnitId.trim().isEmpty() -> {
                listener?.onFailed(key.value, "AdUnit id empty")
                return
            }

            !internetManager.isInternetConnected -> {
                listener?.onFailed(key.value, "No internet")
                return
            }
        }

        // register config for lookups
        registry.putInfo(key, AdInfo(config.adUnitId, config.canShare, config.canReuse, config.bufferSize))

        // Policy: If *any* ad (which is shareable) already loaded and available with no impression, prefer reuse
        // Find any available ad that can be used instead of loading a new ad, but only if allowed by canShare/canReuse.
        val existingReusableKey = findReusableAdFor(key)
        if (existingReusableKey != null) {
            // We won't start a new preload if there's an available ad: prefer to increase show-rate
            listener?.onLoaded(adConfigMap[existingReusableKey]?.adUnitId ?: registry.getInfo(existingReusableKey)!!.adUnitId)
            return
        }

        // else start preload for this key's ad unit
        preloadEngine.startPreload(AdInfo(config.adUnitId, config.canShare, config.canReuse, config.bufferSize), listener)
    }

    fun showAd(activity: Activity?, key: InterAdKey, listener: InterstitialShowListener? = null) {
        val config = adConfigMap[key] ?: run {
            listener?.onAdFailedToShow(key.value, "Unknown key")
            return
        }

        when {
            activity == null -> {
                listener?.onAdFailedToShow(key.value, "Activity Ref is null")
                return
            }
            sharedPrefs.isAppPurchased -> {
                listener?.onAdFailedToShow(key.value, "Premium user")
                return
            }

            config.adUnitId.trim().isEmpty() -> {
                listener?.onAdFailedToShow(key.value, "AdUnit id empty")
                return
            }

            activity.isFinishing || activity.isDestroyed -> {
                listener?.onAdFailedToShow(key.value, "Activity invalid")
                return
            }
        }

        // If this key canReuse==true or the key's own ad is available, prefer own ad.
        // If own ad isn't available but other shareable ad is (and this key allows reuse), use that.
        val ownInfo = registry.getInfo(key)
        val ownUnit = ownInfo?.adUnitId

        if (ownUnit != null && InterstitialAdPreloader.isAdAvailable(ownUnit)) {
            showEngine.showAd(activity, ownUnit, listener)
            return
        }

        // If own is not available: try to find reusable ad (other key) that canShare == true and available
        val reusableKey = findReusableAdFor(key)
        if (reusableKey != null) {
            val unit = registry.getInfo(reusableKey)?.adUnitId
            if (unit != null && InterstitialAdPreloader.isAdAvailable(unit)) {
                showEngine.showAd(activity, unit, listener)
                return
            }
        }

        // No ad available — report failure
        listener?.onAdFailedToShow(key.value, "No available ad to show")
    }

    fun destroyAd(key: InterAdKey) {
        registry.getInfo(key)?.adUnitId?.let { preloadEngine.stopPreload(it) }
        registry.removeInfo(key)
    }

    fun destroyAllAds() {
        // stop all based on registry info
        registry.clearAll()
        preloadEngine.stopAll()
    }

    fun isAdLoaded(key: InterAdKey): Boolean {
        val info = registry.getInfo(key) ?: return false
        return InterstitialAdPreloader.isAdAvailable(info.adUnitId)
    }

    // Helper: find any reusable ad key for the requested key
    private fun findReusableAdFor(requested: InterAdKey): InterAdKey? {

        // Prefer same adUnitId if present and not the same key
        val requestedUnit = registry.getInfo(requested)?.adUnitId
        if (requestedUnit != null) {
            val sameUnit = registry.findAdKeyByUnit(requestedUnit)
            if (
                sameUnit != null &&
                sameUnit != requested &&
                registry.getInfo(sameUnit)?.canShare == true &&
                !registry.wasAdShown(requestedUnit) &&
                InterstitialAdPreloader.isAdAvailable(requestedUnit)
            ) {
                return sameUnit
            }
        }

        // Fallback: any shareable, loaded, not-shown, active ad
        val found = registryEntriesFind { (key, info) ->
            key != requested &&
                    info.canShare &&
                    !registry.wasAdShown(info.adUnitId) &&
                    registry.isPreloadActive(info.adUnitId) &&
                    InterstitialAdPreloader.isAdAvailable(info.adUnitId)
        }?.first   // since Pair<InterAdKey, AdInfo>

        return found
    }

    // --------------------------------------------
    // Pair-based matcher
    // --------------------------------------------
    private fun registryEntriesFind(predicate: (Pair<InterAdKey, AdInfo>) -> Boolean): Pair<InterAdKey, AdInfo>? {
        return registrySnapshot().firstOrNull(predicate)
    }

    // --------------------------------------------
    // Snapshot as List<Pair<InterAdKey, AdInfo>>
    // --------------------------------------------
    private fun registrySnapshot(): List<Pair<InterAdKey, AdInfo>> {
        return adConfigMap.keys.mapNotNull { key ->
            registry.getInfo(key)?.let { info -> key to info }
        }
    }
}