package dev.pegasus.nextgensdk.ads.inter.utils

import android.os.Handler
import android.os.Looper

internal object MainDispatcher {
    private val handler = Handler(Looper.getMainLooper())

    fun dispatch(delay: Long = 0L, action: () -> Unit) {
        if (delay <= 0L) handler.post(action) else handler.postDelayed(action, delay)
    }
}