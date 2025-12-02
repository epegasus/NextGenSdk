package com.hypersoft.admobpreloader.bannerAds.di

import com.hypersoft.admobpreloader.bannerAds.BannerAdsManager
import com.hypersoft.admobpreloader.bannerAds.engine.PreloadEngine
import com.hypersoft.admobpreloader.bannerAds.engine.ShowEngine
import com.hypersoft.admobpreloader.bannerAds.storage.AdRegistry
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin module for BannerAdsManager and its dependencies.
 */
val bannerAdsModule = module {
    single { AdRegistry() }
    single { PreloadEngine(get()) }
    single { ShowEngine(get(), get()) }
    single {
        BannerAdsManager(
            resources = androidContext().resources,
            registry = get(),
            preloadEngine = get(),
            showEngine = get(),
            internetManager = get(),
            sharedPrefs = get()
        )
    }
}