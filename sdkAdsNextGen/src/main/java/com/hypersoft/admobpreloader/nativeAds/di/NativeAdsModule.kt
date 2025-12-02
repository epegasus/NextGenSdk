package com.hypersoft.admobpreloader.nativeAds.di

import com.hypersoft.admobpreloader.nativeAds.NativeAdsManager
import com.hypersoft.admobpreloader.nativeAds.engine.PreloadEngine
import com.hypersoft.admobpreloader.nativeAds.engine.ShowEngine
import com.hypersoft.admobpreloader.nativeAds.storage.AdRegistry
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin module for NativeAdsManager and its dependencies.
 */
val nativeAdsModule = module {
    single { AdRegistry() }
    single { PreloadEngine(get()) }
    single { ShowEngine(get(), get()) }
    single {
        NativeAdsManager(
            resources = androidContext().resources,
            registry = get(),
            preloadEngine = get(),
            showEngine = get(),
            internetManager = get(),
            sharedPrefs = get()
        )
    }
}