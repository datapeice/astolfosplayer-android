package com.datapeice.astolfosplayer.app.data.repository

import com.datapeice.astolfosplayer.app.data.db.PlaylistDao
import com.datapeice.astolfosplayer.app.data.db.PlaylistEntity
import com.datapeice.astolfosplayer.app.domain.track.Playlist
import com.datapeice.astolfosplayer.app.domain.track.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RoomPlaylistRepository(
    private val dao: PlaylistDao
) : PlaylistRepository {

    override fun getPlaylists(): Flow<List<Playlist>> {
        return dao.getAllPlaylists().map { entities ->
            // Превращаем Entities обратно в Playlist и разворачиваем список (как было в Realm версии)
            entities.map { entity ->
                Json.decodeFromString<Playlist>(entity.json)
            }.reversed()
        }
    }

    override suspend fun insertPlaylist(playlist: Playlist) {
        dao.insertOrUpdate(playlist.toEntity())
    }

    override suspend fun updatePlaylistTrackList(playlist: Playlist, trackList: List<Track>) {
        // Создаем копию плейлиста с новым списком треков
        val updatedPlaylist = playlist.copy(trackList = trackList)
        // Сохраняем (Room заменит старую запись, так как имя совпадает)
        dao.insertOrUpdate(updatedPlaylist.toEntity())
    }

    override suspend fun deletePlaylist(playlist: Playlist) {
        playlist.name?.let {
            dao.deleteByName(it)
        }
    }

    override suspend fun renamePlaylist(playlist: Playlist, name: String) {
        // В Room нельзя изменить Primary Key, поэтому удаляем старый и создаем новый
        deletePlaylist(playlist)
        insertPlaylist(playlist.copy(name = name))
    }

    // Вспомогательная функция для конвертации в Entity
    private fun Playlist.toEntity(): PlaylistEntity {
        return PlaylistEntity(
            name = this.name ?: "",
            json = Json.encodeToString(this)
        )
    }
}