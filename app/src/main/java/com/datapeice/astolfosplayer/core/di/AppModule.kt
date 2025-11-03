package com.datapeice.astolfosplayer.core.di

import com.datapeice.astolfosplayer.core.data.MusicScanner
import com.datapeice.astolfosplayer.core.data.Settings
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single<Settings> {
        Settings(context = androidContext())
    }

    single<MusicScanner> {
        MusicScanner(
            context = androidContext(),
            settings = get()
        )
    }
}