package com.datapeice.astolfosplayer.core.di

import com.datapeice.astolfosplayer.core.api.AuthApi
import com.datapeice.astolfosplayer.core.api.GrpcAuthApi
import com.datapeice.astolfosplayer.core.api.GrpcChannelProvider
import com.datapeice.astolfosplayer.core.api.GrpcSyncApi
import com.datapeice.astolfosplayer.core.api.SyncApi
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val apiModule = module {
    // 1. Провайдер канала gRPC
    single {
        GrpcChannelProvider(
            context = androidContext(),
            settings = get()
        )
    }

    // 2. Auth API
    single<AuthApi> {
        GrpcAuthApi(
            context = androidContext(),
            settings = get(),
            channelProvider = get()
        )
    }

    // 3. Sync API
    single<SyncApi> {
        GrpcSyncApi(
            context = androidContext(),
            settings = get(),
            channelProvider = get(),
            trackApi = get() // <--- ИСПРАВЛЕНО: Добавлена недостающая зависимость
        )
    }
}