package dev.pegasus.nextgensdk.ads.inter

import android.app.Activity
import android.content.Context
import dev.pegasus.nextgensdk.utils.network.InternetManager
import dev.pegasus.nextgensdk.utils.storage.SharedPreferencesDataSource

class InterstitialValidator(
    private val internetManager: InternetManager,
    private val prefs: SharedPreferencesDataSource
) {

    fun validateBeforeLoad(ctx: Context): Result<Unit> = when {
        prefs.isAppPurchased -> Result.failure(Exception("Premium users shouldn’t see ads"))
        ctx !is Activity || ctx.isFinishing -> Result.failure(Exception("Invalid activity context"))
        !internetManager.isInternetConnected -> Result.failure(Exception("No internet connection"))
        else -> Result.success(Unit)
    }

    fun validateBeforeShow(ctx: Context): Result<Unit> = when {
        prefs.isAppPurchased -> Result.failure(Exception("Premium users shouldn’t see ads"))
        ctx !is Activity || ctx.isFinishing -> Result.failure(Exception("Invalid activity context"))
        else -> Result.success(Unit)
    }
}