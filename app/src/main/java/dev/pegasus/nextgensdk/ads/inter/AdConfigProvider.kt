package dev.pegasus.nextgensdk.ads.inter

import dev.pegasus.nextgensdk.ads.inter.enums.InterAdKey

class AdConfigProvider {

    private val map = mapOf(
        InterAdKey.HOME to "ca-app-pub-xxxxx/home",
        InterAdKey.DETAIL to "ca-app-pub-xxxxx/detail"
    )

    fun getAdUnitId(key: InterAdKey): String = map[key] ?: error("Missing ad unit")
}