package dev.pegasus.nextgensdk.ads.inter.utils

import android.util.Log
import dev.pegasus.nextgensdk.utils.constants.Constants.TAG_ADS

/**
 * Centralized logging utility for inter package.
 * Follows the same clean logging strategy as interstitialAds package.
 */
internal object AdLogger {
    fun logError(adType: String, method: String, message: String) {
        Log.e(TAG_ADS, "$adType -> $method: $message")
    }

    fun logDebug(adType: String, method: String, message: String) {
        Log.d(TAG_ADS, "$adType -> $method: $message")
    }

    fun logInfo(adType: String, method: String, message: String) {
        Log.i(TAG_ADS, "$adType -> $method: $message")
    }

    fun logVerbose(adType: String, method: String, message: String) {
        Log.v(TAG_ADS, "$adType -> $method: $message")
    }
}