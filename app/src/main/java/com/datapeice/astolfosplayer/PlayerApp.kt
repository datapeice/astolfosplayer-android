package com.datapeice.astolfosplayer

import android.app.Application
import com.datapeice.astolfosplayer.app.di.playerModule
import com.datapeice.astolfosplayer.core.di.apiModule
import com.datapeice.astolfosplayer.core.di.appModule
import com.datapeice.astolfosplayer.setup.di.setupModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PlayerApp: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@PlayerApp)
            modules(appModule, setupModule, playerModule, apiModule)
        }
    }
}