package com.datapeice.astolfoplayer.app.di

import com.datapeice.astolfoplayer.EqualizerController
import com.datapeice.astolfoplayer.app.data.LyricsReader
import com.datapeice.astolfoplayer.app.data.LyricsReaderImpl
import com.datapeice.astolfoplayer.app.data.MetadataWriter
import com.datapeice.astolfoplayer.app.data.MetadataWriterImpl
import com.datapeice.astolfoplayer.app.data.SavedPlayerState
import com.datapeice.astolfoplayer.app.data.remote.lyrics.LrclibLyricsProvider
import com.datapeice.astolfoplayer.app.data.remote.lyrics.LyricsProvider
import com.datapeice.astolfoplayer.app.data.remote.metadata.MetadataProvider
import com.datapeice.astolfoplayer.app.data.remote.metadata.MusicBrainzMetadataProvider
import com.datapeice.astolfoplayer.app.data.repository.LyricsJson
import com.datapeice.astolfoplayer.app.data.repository.LyricsRepository
import com.datapeice.astolfoplayer.app.data.repository.PlaylistJson
import com.datapeice.astolfoplayer.app.data.repository.PlaylistRepository
import com.datapeice.astolfoplayer.app.data.repository.RealmLyricsRepository
import com.datapeice.astolfoplayer.app.data.repository.RealmPlaylistRepository
import com.datapeice.astolfoplayer.app.data.repository.TrackRepository
import com.datapeice.astolfoplayer.app.data.repository.TrackRepositoryImpl
import com.datapeice.astolfoplayer.app.presentation.PlayerViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val playerModule = module {

    single<TrackRepository> {
        TrackRepositoryImpl(
            context = androidContext(),
            settings = get()
        )
    }

    single<SavedPlayerState> {
        SavedPlayerState(
            context = androidContext()
        )
    }

    single<HttpClient> {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    }
                )
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 180000
            }
        }
    }

    single<MetadataProvider> {
        MusicBrainzMetadataProvider(
            context = androidContext(),
            client = get()
        )
    }

    single<MetadataWriter> {
        MetadataWriterImpl(context = androidContext())
    }

    single<LyricsProvider> {
        LrclibLyricsProvider(
            context = androidContext(),
            client = get()
        )
    }

    single<Realm> {
        val configuration = RealmConfiguration.create(
            schema = setOf(LyricsJson::class, PlaylistJson::class)
        )

        Realm.open(configuration)
    }

    single<LyricsRepository> {
        RealmLyricsRepository(
            realm = get()
        )
    }

    single<PlaylistRepository> {
        RealmPlaylistRepository(
            realm = get()
        )
    }

    single<EqualizerController> {
        EqualizerController(
            context = androidContext()
        )
    }

    single<LyricsReader> {
        LyricsReaderImpl(
            context = androidContext()
        )
    }

    viewModel<PlayerViewModel> {
        PlayerViewModel(
            savedPlayerState = get(),
            trackRepository = get(),
            metadataProvider = get(),
            lyricsProvider = get(),
            lyricsRepository = get(),
            lyricsReader = get(),
            playlistRepository = get(),
            unsupportedArtworkEditFormats = get<MetadataWriter>().unsupportedArtworkEditFormats,
            settings = get(),
            musicScanner = get(),
            equalizerController = get()
        )
    }
}