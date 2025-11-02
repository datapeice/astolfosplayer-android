package com.datapeice.astolfoplayer.core.di

import com.datapeice.astolfoplayer.core.data.MusicScanner
import com.datapeice.astolfoplayer.core.data.Settings
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