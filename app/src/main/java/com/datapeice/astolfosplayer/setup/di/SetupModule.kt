package com.datapeice.astolfosplayer.setup.di

import com.datapeice.astolfosplayer.app.data.repository.TrackRepository
import com.datapeice.astolfosplayer.setup.data.SetupState
import com.datapeice.astolfosplayer.setup.presentation.SetupViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val setupModule = module {
    single<SetupState> {
        SetupState(context = androidContext())
    }

    viewModel<SetupViewModel> {
        SetupViewModel(
            setupState = get(),
            settings = get(),
            musicScanner = get(),
            getFoldersWithAudio = get<TrackRepository>()::getFoldersWithAudio,
            authApi = get(),
            context = androidContext()
        )
    }
}