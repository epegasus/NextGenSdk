package dev.pegasus.nextgensdk.inter

import android.content.res.Resources
import androidx.annotation.StringRes
import dev.pegasus.nextgensdk.R
import dev.pegasus.nextgensdk.utils.SharedPreferencesDataSource

class InterstitialAdsConfig(
    private val resources: Resources,
    private val sharedPreferencesDataSource: SharedPreferencesDataSource,
    private val interstitialAdsRemote: InterstitialAdsRemote
) {

    suspend fun loadInterstitialAd(interAdKey: InterAdKey) {
        var interAdId = ""
        var isRemoteEnable = false

        when (interAdKey) {
            InterAdKey.ENTRANCE -> {
                interAdId = getResString(R.string.admob_inter_entrance_id)
                isRemoteEnable = sharedPreferencesDataSource.rcInterEntrance != 0
            }
        }



        interstitialAdsRemote.loadInterstitialAd(interAdKey.value, interAdId)


    }

    suspend fun showInterstitialAd() {

    }

    private fun getResString(@StringRes stringResId: Int) = resources.getString(stringResId)
}

interface InterstitialOnLoadCallBack {
    fun onResponse(successfullyLoaded: Boolean) {}
}

interface InterstitialOnShowCallBack {
    fun onAdShowedFullScreenContent() {}
    fun onAdDismissedFullScreenContent() {}
    fun onAdFailedToShow()
    fun onAdImpression() {}
    fun onAdImpressionDelayed() {}
    fun onAdClicked() {}
}