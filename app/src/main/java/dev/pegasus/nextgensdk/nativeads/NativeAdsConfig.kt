package dev.pegasus.nextgensdk.nativeads

import android.content.res.Resources
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import dev.pegasus.nextgensdk.R
import dev.pegasus.nextgensdk.nativeads.callbacks.NativeOnLoadCallback
import dev.pegasus.nextgensdk.nativeads.callbacks.NativeOnShowCallback
import dev.pegasus.nextgensdk.nativeads.enums.NativeAdKey
import dev.pegasus.nextgensdk.nativeads.manager.NativeAdsManager
import dev.pegasus.nextgensdk.utils.network.InternetManager
import dev.pegasus.nextgensdk.utils.storage.SharedPreferencesDataSource
import java.util.concurrent.ConcurrentHashMap

class NativeAdsConfig(
    private val resources: Resources,
    private val sharedPreferencesDataSource: SharedPreferencesDataSource,
    internetManager: InternetManager
) : NativeAdsManager(sharedPreferencesDataSource, internetManager) {

    private data class AdConfig(
        val adUnitId: String,
        val isRemoteEnable: Boolean,
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
            canShare = true,
            canReuse = true
        )

        NativeAdKey.ON_BOARDING -> AdConfig(
            adUnitId = resources.getString(R.string.admob_native_language_id),
            isRemoteEnable = sharedPreferencesDataSource.rcNativeOnBoarding != 0,
            canShare = false,
            canReuse = false
        )

        NativeAdKey.DASHBOARD -> AdConfig(
            adUnitId = resources.getString(R.string.admob_native_language_id),
            isRemoteEnable = sharedPreferencesDataSource.rcNativeHome != 0,
            canShare = false,
            canReuse = false
        )

        NativeAdKey.FEATURE -> AdConfig(
            adUnitId = resources.getString(R.string.admob_native_feature_id),
            isRemoteEnable = sharedPreferencesDataSource.rcNativeFeature != 0,
            canShare = false,
            canReuse = false
        )

        NativeAdKey.EXIT -> AdConfig(
            adUnitId = resources.getString(R.string.admob_native_exit_id),
            isRemoteEnable = sharedPreferencesDataSource.rcNativeExit != 0,
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
        startPreloadingNative(
            adType = key.value,
            adUnitId = cfg.adUnitId,
            isRemoteEnable = cfg.isRemoteEnable
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
        val ad = pollNativeAd(key.value, adUnitId) ?: return null
        attachShowCallbacks(key.value, adUnitId, ad, showCallback)
        return ad
    }

    fun clearNativeAd(key: NativeAdKey) {
        val adUnitId = adInfoMap.remove(key)
        if (adUnitId != null && wasNativeShown(adUnitId)) {
            // nothing else to do; state is tracked in manager
        }
    }
}