package dev.pegasus.nextgensdk.di

import com.hypersoft.admobpreloader.interstitialAds.di.interstitialAdsModule
import com.hypersoft.core.di.coreModules
import dev.pegasus.nextgensdk.ads.nativeAds.NativeAdsConfig
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val nativeAdsModule = module {
    single {
        NativeAdsConfig(
            resources = androidContext().resources,
            sharedPreferencesDataSource = get(),
            internetManager = get()
        )
    }
}

val appModules = listOf(
    coreModules,
    interstitialAdsModule,
    nativeAdsModule
)