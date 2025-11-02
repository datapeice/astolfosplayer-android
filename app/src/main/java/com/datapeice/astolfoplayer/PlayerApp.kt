package com.datapeice.astolfoplayer

import android.app.Application
import com.datapeice.astolfoplayer.app.di.playerModule
import com.datapeice.astolfoplayer.core.di.appModule
import com.datapeice.astolfoplayer.setup.di.setupModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PlayerApp: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@PlayerApp)
            modules(appModule, setupModule, playerModule)
        }
    }
}