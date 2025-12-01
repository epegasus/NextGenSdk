package dev.pegasus.nextgensdk.ads.inter

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import dev.pegasus.nextgensdk.ads.inter.enums.InterAdKey

class InterstitialShowEngine(
    private val context: Context,
    private val adStorage: AdStorage,
) {

    fun show(key: InterAdKey, callback: ShowCallback) {
        val ad = adStorage.get(key)
        if (ad == null) {
            callback.onFailed("Ad not loaded")
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                callback.onShown()
            }

            override fun onAdFailedToShowFullScreenContent(e: AdError) {
                callback.onFailed(e.message)
            }

            override fun onAdDismissedFullScreenContent() {
                adStorage.remove(key)
                callback.onDismissed()
            }
        }

        ad.show(context as Activity)
    }
}
