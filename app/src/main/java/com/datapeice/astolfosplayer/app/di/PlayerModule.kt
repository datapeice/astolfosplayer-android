package com.datapeice.astolfosplayer.app.di

import androidx.room.Room
import com.datapeice.astolfosplayer.EqualizerController
import com.datapeice.astolfosplayer.app.data.LyricsReader
import com.datapeice.astolfosplayer.app.data.LyricsReaderImpl
import com.datapeice.astolfosplayer.app.data.MetadataWriter
import com.datapeice.astolfosplayer.app.data.MetadataWriterImpl
import com.datapeice.astolfosplayer.app.data.SavedPlayerState
import com.datapeice.astolfosplayer.app.data.db.AppDatabase
import com.datapeice.astolfosplayer.app.data.remote.lyrics.LrclibLyricsProvider
import com.datapeice.astolfosplayer.app.data.remote.lyrics.LyricsProvider
import com.datapeice.astolfosplayer.app.data.remote.metadata.MetadataProvider
import com.datapeice.astolfosplayer.app.data.remote.metadata.MusicBrainzMetadataProvider
import com.datapeice.astolfosplayer.app.data.repository.LyricsRepository
import com.datapeice.astolfosplayer.app.data.repository.PlaylistRepository
import com.datapeice.astolfosplayer.app.data.repository.RoomLyricsRepository
import com.datapeice.astolfosplayer.app.data.repository.RoomPlaylistRepository
import com.datapeice.astolfosplayer.app.data.repository.TrackIdStorage
import com.datapeice.astolfosplayer.app.data.repository.TrackRepository
import com.datapeice.astolfosplayer.app.data.repository.TrackRepositoryImpl
import com.datapeice.astolfosplayer.app.presentation.PlayerViewModel
import com.datapeice.astolfosplayer.core.api.GrpcTrackApi
import com.datapeice.astolfosplayer.core.api.TrackApi
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val playerModule = module {

    single { TrackIdStorage(androidContext()) }

    single<TrackRepository> {
        TrackRepositoryImpl(
            context = androidContext(),
            settings = get(),
            trackIdStorage = get()
        )
    }

    single<SavedPlayerState> {
        SavedPlayerState(
            context = androidContext()
        )
    }

    single<TrackApi> {
        GrpcTrackApi(
            context = androidContext(),
            settings = get(),
            channelProvider = get()
        )
    }

    // ИСПРАВЛЕНИЕ: Используем OkHttp вместо CIO
    single<HttpClient> {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
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

    // --- НАЧАЛО: ИЗМЕНЕНИЯ ROOM ---

    // 1. Создание базы данных
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "astolfos_player.db"
        )
            .fallbackToDestructiveMigration() // Удалит старую базу при изменении структуры
            .build()
    }

    // 2. Предоставление DAO
    single { get<AppDatabase>().playlistDao() }
    single { get<AppDatabase>().lyricsDao() }

    // 3. Репозитории теперь используют DAO и новые Room реализации
    single<LyricsRepository> {
        RoomLyricsRepository(
            dao = get()
        )
    }

    single<PlaylistRepository> {
        RoomPlaylistRepository(
            dao = get()
        )
    }
    // --- КОНЕЦ: ИЗМЕНЕНИЯ ROOM ---

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
            syncApi = get(),
            lyricsRepository = get(),
            lyricsReader = get(),
            playlistRepository = get(),
            // ИСПРАВЛЕНО: добавлен .toSet() для конвертации List в Set
            unsupportedArtworkEditFormats = get<MetadataWriter>().unsupportedArtworkEditFormats.toSet(),
            settings = get(),
            musicScanner = get(),
            equalizerController = get(),
            setupViewModel = get(),
            context = get(),
            trackApi = get()
        )
    }
}