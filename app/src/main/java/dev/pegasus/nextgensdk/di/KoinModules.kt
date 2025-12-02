package dev.pegasus.nextgensdk.di

import android.content.Context
import android.net.ConnectivityManager
import dev.pegasus.nextgensdk.ads.inter.InterstitialAdsManager
import dev.pegasus.nextgensdk.ads.inter.engine.PreloadEngine
import dev.pegasus.nextgensdk.ads.inter.engine.ShowEngine
import dev.pegasus.nextgensdk.ads.inter.storage.AdRegistry
import dev.pegasus.nextgensdk.ads.interstitialAds.InterstitialAdsConfig
import dev.pegasus.nextgensdk.ads.nativeAds.NativeAdsConfig
import dev.pegasus.nextgensdk.utils.network.InternetManager
import dev.pegasus.nextgensdk.utils.storage.SharedPreferencesDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private val externalModule = module {
    single { androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    single { androidContext().getSharedPreferences("app_preferences", Context.MODE_PRIVATE) }
}

private val dataSourceModule = module {
    single { SharedPreferencesDataSource(get()) }
}

private val managerModule = module {
    single { InternetManager(get()) }
}

private val adsModule = module {
    single { AdRegistry() }
    single { PreloadEngine(get()) }
    single { ShowEngine(get(), get()) }

    single {
        InterstitialAdsManager(
            resources = androidContext().resources,
            registry = get(),
            preloadEngine = get(),
            showEngine = get(),
            internetManager = get(),
            sharedPrefs = get(),
        )
    }
    single {
        InterstitialAdsConfig(
            resources = androidContext().resources,
            sharedPreferencesDataSource = get(),
            internetManager = get()
        )
    }

    single {
        NativeAdsConfig(
            resources = androidContext().resources,
            sharedPreferencesDataSource = get(),
            internetManager = get()
        )
    }
}

val appModules = listOf(
    externalModule,
    dataSourceModule,
    managerModule,
    adsModule
)