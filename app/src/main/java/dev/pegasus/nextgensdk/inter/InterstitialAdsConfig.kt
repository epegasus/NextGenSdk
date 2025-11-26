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
import dev.pegasus.nextgensdk.utils.constants.Constants.TAG_ADS
import dev.pegasus.nextgensdk.utils.network.InternetManager
import dev.pegasus.nextgensdk.utils.storage.SharedPreferencesDataSource

class InterstitialAdsConfig(
    private val resources: Resources,
    sharedPreferencesDataSource: SharedPreferencesDataSource,
    internetManager: InternetManager
) : InterstitialAdsManager(sharedPreferencesDataSource, internetManager) {

    private val counterMap by lazy { HashMap<String, Int>() }
    private val adUnitIdMap = mutableMapOf<String, String>()
    private val reuseAdMap = mutableMapOf<String, Boolean>()

    fun loadInterstitialAd(
        adType: InterAdKey,
        listener: InterstitialOnLoadCallBack? = null
    ) {
        var interAdId: String
        var isRemoteEnable: Boolean
        var bufferSize: Int?
        var reuseAd: Boolean

        when (adType) {
            InterAdKey.ENTRANCE -> {
                interAdId = getResString(R.string.admob_inter_entrance_id)
                isRemoteEnable = sharedPreferencesDataSource.rcInterEntrance != 0
                bufferSize = null
                reuseAd = true
            }

            InterAdKey.ON_BOARDING -> {
                interAdId = getResString(R.string.admob_inter_on_boarding_id)
                isRemoteEnable = sharedPreferencesDataSource.rcInterOnBoarding != 0
                bufferSize = null
                reuseAd = true
            }

            InterAdKey.DASHBOARD -> {
                interAdId = getResString(R.string.admob_inter_dashboard_id)
                isRemoteEnable = sharedPreferencesDataSource.rcInterDashboard != 0
                bufferSize = 1
                reuseAd = true
            }
        }

        adUnitIdMap[adType.value] = interAdId
        reuseAdMap[adType.value] = reuseAd

        startPreloading(
            adType = adType.value,
            adUnitId = interAdId,
            isRemoteEnable = isRemoteEnable,
            bufferSize = bufferSize,
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
        val reuseAd = reuseAdMap[adType.value] == true

        if (reuseAd) {
            val reusableAd = findReusableAd(adType)
            if (reusableAd != null) {
                Log.d(TAG_ADS, "${adType.value} -> showInterstitialAd: Reusing available ad from ${reusableAd.first}")
                showPreloadedAd(
                    activity = activity,
                    adType = reusableAd.first,
                    adUnitId = reusableAd.second,
                    listener = listener
                )
                return
            }
        }

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

    private fun findReusableAd(requestedAdType: InterAdKey): Pair<String, String>? {
        for ((adTypeValue, adUnitId) in adUnitIdMap) {
            if (adTypeValue == requestedAdType.value) continue
            if (reuseAdMap[adTypeValue] != true) continue
            if (wasAdShown(adUnitId)) continue
            if (isInterstitialAvailable(adUnitId)) {
                return Pair(adTypeValue, adUnitId)
            }
        }
        return null
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