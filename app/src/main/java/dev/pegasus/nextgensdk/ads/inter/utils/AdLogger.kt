package dev.pegasus.nextgensdk.ads.inter.utils

import android.util.Log
import dev.pegasus.nextgensdk.utils.constants.Constants.TAG_ADS


internal object AdLogger {
    enum class Level { E, W, I, D, V }

    fun log(level: Level, adType: String, method: String, message: String) {
        val tag = TAG_ADS
        val full = "$adType -> $method: $message"
        when (level) {
            Level.E -> Log.e(tag, full)
            Level.W -> Log.w(tag, full)
            Level.I -> Log.i(tag, full)
            Level.D -> Log.d(tag, full)
            Level.V -> Log.v(tag, full)
        }
    }
}