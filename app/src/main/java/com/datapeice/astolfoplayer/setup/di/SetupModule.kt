package com.datapeice.astolfoplayer.setup.di

import com.datapeice.astolfoplayer.app.data.repository.TrackRepository
import com.datapeice.astolfoplayer.setup.data.SetupState
import com.datapeice.astolfoplayer.setup.presentation.SetupViewModel
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
            getFoldersWithAudio = get<TrackRepository>()::getFoldersWithAudio
        )
    }
}