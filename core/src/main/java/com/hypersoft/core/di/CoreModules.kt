package com.hypersoft.core.di

import android.content.Context
import android.net.ConnectivityManager
import com.hypersoft.core.network.InternetManager
import com.hypersoft.core.storage.SharedPreferencesDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModules = module {
    single { androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    single { androidContext().getSharedPreferences("app_preferences", Context.MODE_PRIVATE) }

    single { InternetManager(get()) }
    single { SharedPreferencesDataSource(get()) }
}