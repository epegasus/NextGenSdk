package dev.pegasus.nextgensdk.ads.inter

import android.content.Context
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import dev.pegasus.nextgensdk.ads.inter.enums.InterAdKey

class InterstitialPreloadEngine(
    private val context: Context,
    private val adStorage: AdStorage,
    private val adConfig: AdConfigProvider
) {

    fun load(key: InterAdKey, callback: LoadCallback?) {
        val adUnit = adConfig.getAdUnitId(key)

        val request = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            adUnit,
            request,
            object : InterstitialAdLoadCallback() {

                override fun onAdLoaded(ad: InterstitialAd) {
                    adStorage.put(key, ad)
                    callback?.onLoaded()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    callback?.onFailed(error.message)
                }
            }
        )
    }

    fun destroy(key: InterAdKey) {
        adStorage.remove(key) // GC handles actual destruction
    }

    fun destroyAll() {
        adStorage.clear()
    }
}
