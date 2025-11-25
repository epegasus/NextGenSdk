package dev.pegasus.nextgensdk.inter

import android.app.Activity
import android.content.res.Resources
import android.util.Log
import androidx.annotation.StringRes
import dev.pegasus.nextgensdk.R
import dev.pegasus.nextgensdk.inter.callbacks.InterstitialOnLoadCallBack
import dev.pegasus.nextgensdk.inter.callbacks.InterstitialOnShowCallBack
import dev.pegasus.nextgensdk.inter.enums.InterAdKey
import dev.pegasus.nextgensdk.inter.manager.InterstitialAdsManager
import dev.pegasus.nextgensdk.utils.Constants.TAG_ADS
import dev.pegasus.nextgensdk.utils.InternetManager
import dev.pegasus.nextgensdk.utils.SharedPreferencesDataSource

class InterstitialAdsConfig(
    private val resources: Resources,
    sharedPreferencesDataSource: SharedPreferencesDataSource,
    internetManager: InternetManager
) : InterstitialAdsManager(sharedPreferencesDataSource, internetManager) {

    private val counterMap by lazy { HashMap<String, Int>() }

    private val adUnitIdMap = mutableMapOf<String, String>()

    fun loadInterstitialAd(adType: InterAdKey, listener: InterstitialOnLoadCallBack? = null) {
        var interAdId: String
        var isRemoteEnable: Boolean

        when (adType) {
            InterAdKey.ENTRANCE -> {
                interAdId = getResString(R.string.admob_inter_entrance_id)
                isRemoteEnable = sharedPreferencesDataSource.rcInterEntrance != 0
            }
            InterAdKey.LANGUAGE -> {
                interAdId = getResString(R.string.admob_inter_language_id)
                isRemoteEnable = sharedPreferencesDataSource.rcInterLanguage != 0
            }
        }

        adUnitIdMap[adType.value] = interAdId

        startPreloading(
            adType = adType.value,
            adUnitId = interAdId,
            isRemoteEnable = isRemoteEnable,
            bufferSize = 1,
            listener = listener
        )
    }

    fun loadInterstitialAd(
        adType: InterAdKey,
        remoteCounter: Int,
        loadOnStart: Boolean,
        listener: InterstitialOnLoadCallBack? = null
    ) {
        when (loadOnStart) {
            true -> counterMap.putIfAbsent(adType.value, remoteCounter - 1)
            false -> counterMap.putIfAbsent(adType.value, 0)
        }

        if (counterMap.containsKey(adType.value)) {
            val counter = counterMap[adType.value] ?: 0
            counterMap[adType.value] = counter + 1
            counterMap[adType.value]?.let { currentCounter ->
                Log.d(TAG_ADS, "${adType.value} -> loadInterstitial_Counter ----- Total Counter: $remoteCounter, Current Counter: $currentCounter")
                if (currentCounter >= remoteCounter - 1) {
                    counterMap[adType.value] = 0
                    loadInterstitialAd(adType = adType, listener = listener)
                    return
                }
            }
        }

        listener?.onResponse(false)
    }

    fun showInterstitialAd(activity: Activity?, adType: InterAdKey, listener: InterstitialOnShowCallBack? = null) {
        val adUnitId = adUnitIdMap[adType.value] ?: run {
            Log.e(TAG_ADS, "${adType.value} -> showInterstitialAd: Ad unit ID not found. Make sure to load ad first.")
            listener?.onAdFailedToShow()
            return
        }

        showPreloadedAd(
            activity = activity,
            adType = adType.value,
            adUnitId = adUnitId,
            listener = listener
        )
    }

    fun isInterstitialAdLoaded(adType: InterAdKey): Boolean {
        val adUnitId = adUnitIdMap[adType.value] ?: return false
        return isInterstitialAvailable(adUnitId)
    }

    fun stopPreloading(adType: InterAdKey) {
        val adUnitId = adUnitIdMap[adType.value] ?: return
        stopPreloading(adUnitId)
    }

    private fun getResString(@StringRes resId: Int): String {
        return resources.getString(resId)
    }
}