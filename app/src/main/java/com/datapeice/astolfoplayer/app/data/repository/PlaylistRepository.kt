package com.datapeice.astolfoplayer.app.data.repository

import com.datapeice.astolfoplayer.app.domain.track.Playlist
import com.datapeice.astolfoplayer.app.domain.track.Track
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun insertPlaylist(playlist: Playlist)
    suspend fun updatePlaylistTrackList(playlist: Playlist, trackList: List<Track>)
    suspend fun renamePlaylist(playlist: Playlist, name: String)
    suspend fun deletePlaylist(playlist: Playlist)
}