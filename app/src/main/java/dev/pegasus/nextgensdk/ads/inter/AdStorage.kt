package dev.pegasus.nextgensdk.ads.inter

import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import dev.pegasus.nextgensdk.ads.inter.enums.InterAdKey

class AdStorage {
    private val map = mutableMapOf<InterAdKey, InterstitialAd>()

    fun put(key: InterAdKey, ad: InterstitialAd) { map[key] = ad }
    fun get(key: InterAdKey) = map[key]
    fun remove(key: InterAdKey) { map.remove(key) }
    fun clear() { map.clear() }
}