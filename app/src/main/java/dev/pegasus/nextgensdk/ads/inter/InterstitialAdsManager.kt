package dev.pegasus.nextgensdk.ads.inter

import android.content.Context
import dev.pegasus.nextgensdk.ads.inter.enums.InterAdKey

class InterstitialAdsManager(
    private val context: Context,
    private val preloadEngine: InterstitialPreloadEngine,
    private val showEngine: InterstitialShowEngine,
    private val validator: InterstitialValidator
) {

    fun loadAd(key: InterAdKey, callback: LoadCallback? = null) {
        validator.validateBeforeLoad(context).onFailure { error ->
            callback?.onFailed(error)
            return
        }
        preloadEngine.load(key, callback)
    }

    fun showAd(key: InterAdKey, callback: ShowCallback) {
        validator.validateBeforeShow(context).onFailure { error ->
            callback.onFailed(error)
            return
        }
        showEngine.show(key, callback)
    }

    fun destroyAd(key: InterAdKey) {
        preloadEngine.destroy(key)
    }

    fun destroyAllAds() {
        preloadEngine.destroyAll()
    }
}