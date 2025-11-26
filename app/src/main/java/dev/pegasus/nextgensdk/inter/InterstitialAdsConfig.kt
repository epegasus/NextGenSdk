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
import java.util.concurrent.ConcurrentHashMap

class InterstitialAdsConfig(
    private val resources: Resources,
    sharedPreferencesDataSource: SharedPreferencesDataSource,
    internetManager: InternetManager
) : InterstitialAdsManager(sharedPreferencesDataSource, internetManager) {

    private val counterMap = ConcurrentHashMap<String, Int>()
    private val adUnitIdMap = ConcurrentHashMap<String, String>()
    private val reuseAdMap = ConcurrentHashMap<String, Boolean>()

    private fun getAdConfig(adType: InterAdKey): AdConfig? {
        return when (adType) {
            InterAdKey.ENTRANCE -> AdConfig(
                adUnitId = getResString(R.string.admob_inter_entrance_id),
                isRemoteEnable = sharedPreferencesDataSource.rcInterEntrance != 0,
                bufferSize = null,
                reuseAd = true
            )

            InterAdKey.ON_BOARDING -> AdConfig(
                adUnitId = getResString(R.string.admob_inter_on_boarding_id),
                isRemoteEnable = sharedPreferencesDataSource.rcInterOnBoarding != 0,
                bufferSize = null,
                reuseAd = true
            )

            InterAdKey.DASHBOARD -> AdConfig(
                adUnitId = getResString(R.string.admob_inter_dashboard_id),
                isRemoteEnable = sharedPreferencesDataSource.rcInterDashboard != 0,
                bufferSize = 1,
                reuseAd = true
            )
        }
    }

    fun loadInterstitialAd(
        adType: InterAdKey,
        listener: InterstitialOnLoadCallBack? = null
    ) {
        val config = getAdConfig(adType) ?: run {
            Log.e(TAG_ADS, "${adType.value} -> loadInterstitialAd: Unknown ad type")
            listener?.onResponse(false)
            return
        }

        if (config.reuseAd) {
            val reusableAd = findReusableAd(adType, config.adUnitId)
            if (reusableAd != null) {
                Log.d(TAG_ADS, "${adType.value} -> loadInterstitialAd: Reusable ad available from ${reusableAd.first}, skipping load")
                adUnitIdMap[adType.value] = reusableAd.second
                reuseAdMap[adType.value] = config.reuseAd
                listener?.onResponse(true)
                return
            }
        }

        adUnitIdMap[adType.value] = config.adUnitId
        reuseAdMap[adType.value] = config.reuseAd

        startPreloading(
            adType = adType.value,
            adUnitId = config.adUnitId,
            isRemoteEnable = config.isRemoteEnable,
            bufferSize = config.bufferSize,
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

        val currentCounter = counterMap[adType.value] ?: 0
        counterMap[adType.value] = currentCounter + 1

        Log.d(TAG_ADS, "${adType.value} -> loadInterstitial_Counter ----- Total Counter: $remoteCounter, Current Counter: ${currentCounter + 1}")

        if (currentCounter + 1 >= remoteCounter) {
            counterMap[adType.value] = 0
            loadInterstitialAd(adType = adType, listener = listener)
            return
        }

        listener?.onResponse(false)
    }

    fun showInterstitialAd(activity: Activity?, adType: InterAdKey, listener: InterstitialOnShowCallBack? = null) {
        val reuseAd = reuseAdMap[adType.value] == true
        val requestedAdUnitId = adUnitIdMap[adType.value]

        if (reuseAd) {
            val reusableAd = findReusableAd(adType, requestedAdUnitId)
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

        val adUnitId = requestedAdUnitId ?: run {
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

    private fun findReusableAd(requestedAdType: InterAdKey, requestedAdUnitId: String? = null): Pair<String, String>? {
        if (requestedAdUnitId != null) {
            for ((adTypeValue, adUnitId) in adUnitIdMap) {
                if (adTypeValue == requestedAdType.value) continue
                if (adUnitId == requestedAdUnitId && isInterstitialAvailable(adUnitId) && !wasAdShown(adUnitId)) {
                    Log.d(TAG_ADS, "${requestedAdType.value} -> findReusableAd: Found same ad unit ID from $adTypeValue")
                    return Pair(adTypeValue, adUnitId)
                }
            }
        }

        for ((adTypeValue, adUnitId) in adUnitIdMap) {
            if (adTypeValue == requestedAdType.value) continue
            if (wasAdShown(adUnitId)) continue
            if (isInterstitialAvailable(adUnitId)) {
                return Pair(adTypeValue, adUnitId)
            }
        }

        return null
    }

    fun isInterstitialAdLoaded(adType: InterAdKey): Boolean {
        val reuseAd = reuseAdMap[adType.value] == true
        val requestedAdUnitId = adUnitIdMap[adType.value]

        if (reuseAd) {
            val reusableAd = findReusableAd(adType, requestedAdUnitId)
            if (reusableAd != null) {
                return true
            }
        }

        val adUnitId = requestedAdUnitId ?: return false
        return isInterstitialAvailable(adUnitId)
    }

    fun stopPreloading(adType: InterAdKey) {
        val adUnitId = adUnitIdMap[adType.value] ?: return
        stopPreloading(adUnitId)
    }

    fun clearAdData(adType: InterAdKey) {
        val adUnitId = adUnitIdMap.remove(adType.value)
        reuseAdMap.remove(adType.value)
        counterMap.remove(adType.value)
        adUnitId?.let { stopPreloading(it) }
    }

    private fun getResString(@StringRes resId: Int): String {
        return resources.getString(resId)
    }

    private data class AdConfig(
        val adUnitId: String,
        val isRemoteEnable: Boolean,
        val bufferSize: Int?,
        val reuseAd: Boolean
    )
}