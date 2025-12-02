package com.hypersoft.admobpreloader.bannerAds.model

/**
 * Runtime info for a banner placement, stored in the registry.
 */
data class AdInfo(
    val adUnitId: String,
    val canShare: Boolean,
    val canReuse: Boolean,
    val bufferSize: Int?
)