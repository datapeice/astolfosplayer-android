package com.datapeice.astolfosplayer.core.di

import com.datapeice.astolfosplayer.core.api.AuthApi
import com.datapeice.astolfosplayer.core.api.KtorAuthApi
import com.datapeice.astolfosplayer.core.api.KtorSyncApi
import com.datapeice.astolfosplayer.core.api.KtorTrackApi
import com.datapeice.astolfosplayer.core.api.SyncApi
import com.datapeice.astolfosplayer.core.api.TrackApi
import org.koin.dsl.module

val apiModule = module {
    // API для аутентификации
    single<AuthApi> { KtorAuthApi(httpClient = get()) }

    // API для работы с треками
    single<TrackApi> { KtorTrackApi(httpClient = get(), settings = get()) }

    // API для синхронизации
    single<SyncApi> { KtorSyncApi(httpClient = get(), settings = get()) }
}
