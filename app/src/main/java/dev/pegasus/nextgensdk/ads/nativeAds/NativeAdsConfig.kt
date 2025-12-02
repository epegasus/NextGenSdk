package dev.pegasus.nextgensdk.ads.nativeAds

import android.content.res.Resources
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.hypersoft.core.network.InternetManager
import com.hypersoft.core.storage.SharedPreferencesDataSource
import dev.pegasus.nextgensdk.R
import dev.pegasus.nextgensdk.ads.nativeAds.callbacks.NativeOnLoadCallback
import dev.pegasus.nextgensdk.ads.nativeAds.callbacks.NativeOnShowCallback
import dev.pegasus.nextgensdk.ads.nativeAds.enums.NativeAdKey
import dev.pegasus.nextgensdk.ads.nativeAds.manager.NativeAdsManager
import java.util.concurrent.ConcurrentHashMap

class NativeAdsConfig(
    private val resources: Resources,
    private val sharedPreferencesDataSource: SharedPreferencesDataSource,
    internetManager: InternetManager
) : NativeAdsManager(sharedPreferencesDataSource, internetManager) {

    private data class AdConfig(
        val adUnitId: String,
        val isRemoteEnable: Boolean,
        val bufferSize: Int?,       // mirrors interstitial buffer logic
        val canShare: Boolean,
        val canReuse: Boolean,
    )

    // key -> current adUnitId for that placement
    private val adInfoMap = ConcurrentHashMap<NativeAdKey, String>()
    private val loadCallbackMap = ConcurrentHashMap<NativeAdKey, NativeOnLoadCallback>()

    private fun getAdConfig(key: NativeAdKey): AdConfig = when (key) {
        NativeAdKey.LANGUAGE -> AdConfig(
            adUnitId = resources.getString(R.string.admob_native_language_id),
            isRemoteEnable = sharedPreferencesDataSource.rcNativeLanguage != 0,
            bufferSize = null,   // single ad at a time
            canShare = true,
            canReuse = true
        )

        NativeAdKey.ON_BOARDING -> AdConfig(
            adUnitId = resources.getString(R.string.admob_native_language_id),
            isRemoteEnable = sharedPreferencesDataSource.rcNativeOnBoarding != 0,
            bufferSize = null,
            canShare = false,
            canReuse = false
        )

        NativeAdKey.DASHBOARD -> AdConfig(
            adUnitId = resources.getString(R.string.admob_native_language_id),
            isRemoteEnable = sharedPreferencesDataSource.rcNativeHome != 0,
            bufferSize = 1,      // dashboard can benefit from 1 buffered ad
            canShare = false,
            canReuse = false
        )

        NativeAdKey.FEATURE -> AdConfig(
            adUnitId = resources.getString(R.string.admob_native_feature_id),
            isRemoteEnable = sharedPreferencesDataSource.rcNativeFeature != 0,
            bufferSize = null,
            canShare = false,
            canReuse = false
        )

        NativeAdKey.EXIT -> AdConfig(
            adUnitId = resources.getString(R.string.admob_native_exit_id),
            isRemoteEnable = sharedPreferencesDataSource.rcNativeExit != 0,
            bufferSize = null,
            canShare = false,
            canReuse = false
        )
    }

    /** LANGUAGE can be preloaded ahead (EntranceFragment), others on demand. */
    fun loadNativeAd(key: NativeAdKey, listener: NativeOnLoadCallback? = null) {
        val cfg = getAdConfig(key)

        // Replace any previous listener for this key (Entrance vs Language, etc.)
        if (listener != null) {
            loadCallbackMap[key] = listener
        }

        // Simple v1: no cross‑reuse; each key uses its own logical slot.
        loadNativeAd(
            adType = key.value,
            adUnitId = cfg.adUnitId,
            isRemoteEnable = cfg.isRemoteEnable,
            bufferSize = cfg.bufferSize
        ) { isLoaded ->
            if (isLoaded) {
                adInfoMap[key] = cfg.adUnitId
            }
            // Always notify the latest registered listener for this key
            loadCallbackMap[key]?.onResponse(isLoaded)
        }
    }

    /**
     * Polls and returns a preloaded NativeAd.
     * Caller must:
     *  - inflate your native layout
     *  - bind assets
     *  - call nativeAdView.registerNativeAd(...)
     *  - destroy the ad in onDestroyView()
     */
    fun pollNativeAd(
        key: NativeAdKey,
        showCallback: NativeOnShowCallback? = null
    ): NativeAd? {
        val adUnitId = adInfoMap[key] ?: return null
        return pollNativeAd(key.value, adUnitId, showCallback) ?: return null
    }

    fun clearNativeAd(key: NativeAdKey) {
        val adUnitId = adInfoMap.remove(key)
        if (adUnitId != null && wasNativeShown(adUnitId)) {
            // nothing else to do; state is tracked in manager
        }
    }
}