package dev.pegasus.nextgensdk.di

import android.content.Context
import android.net.ConnectivityManager
import dev.pegasus.nextgensdk.inter.InterstitialAdsConfig
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
    single {
        InterstitialAdsConfig(
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