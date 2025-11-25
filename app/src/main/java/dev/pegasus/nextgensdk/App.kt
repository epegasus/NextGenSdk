package dev.pegasus.nextgensdk

import android.app.Application
import dev.pegasus.nextgensdk.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin()
    }

    private fun initKoin() {
        GlobalContext.startKoin {
            androidContext(this@App)
            modules(appModules)
        }
    }
}