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
    sharedPreferencesDataSource: SharedPreferencesDataSource,
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

    private fun getAdConfig(key: NativeAdKey): AdConfig = when (key) {
        NativeAdKey.LANGUAGE -> AdConfig(
            adUnitId = resources.getString(R.string.admob_native_language_id),
            isRemoteEnable = true,   // plug RC when you add it
            canShare = true,
            canReuse = true
        )
        NativeAdKey.ON_BOARDING -> AdConfig(
            adUnitId = resources.getString(R.string.admob_native_language_id), // same test ID in debug
            isRemoteEnable = true,
            canShare = false,
            canReuse = false
        )
        NativeAdKey.DASHBOARD -> AdConfig(
            adUnitId = resources.getString(R.string.admob_native_language_id), // same test ID in debug
            isRemoteEnable = true,
            canShare = false,
            canReuse = false
        )
    }

    /** LANGUAGE will be preloaded ahead (EntranceFragment), others on demand. */
    fun loadNativeAd(key: NativeAdKey, listener: NativeOnLoadCallback? = null) {
        val cfg = getAdConfig(key)

        // Simple v1: no cross‑reuse; LANGUAGE/ON_BOARDING/DASHBOARD each use their own logical slot.
        startPreloadingNative(
            adType = key.value,
            adUnitId = cfg.adUnitId,
            isRemoteEnable = cfg.isRemoteEnable
        ) { isLoaded ->
            if (isLoaded) {
                adInfoMap[key] = cfg.adUnitId
            }
            listener?.onResponse(isLoaded)
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