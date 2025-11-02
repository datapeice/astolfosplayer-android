package com.datapeice.astolfoplayer.app.data.repository

import com.datapeice.astolfoplayer.app.domain.track.Playlist
import com.datapeice.astolfoplayer.app.domain.track.Track
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RealmPlaylistRepository(
    private val realm: Realm
): PlaylistRepository {
    override fun getPlaylists(): Flow<List<Playlist>> {
        return realm.query<PlaylistJson>().asFlow().map { it.list.reversed().map { it.toPlaylist() } }
    }

    override suspend fun insertPlaylist(playlist: Playlist) {
        realm.write {
            copyToRealm(instance = playlist.toPlaylistJson(), updatePolicy = UpdatePolicy.ALL)
        }
    }

    override suspend fun updatePlaylistTrackList(playlist: Playlist, trackList: List<Track>) {
        realm.write {
            val queryResult = query<PlaylistJson>(query = "name = $0", playlist.name!!).find().firstOrNull()
            queryResult?.let {
                findLatest(it)?.json = playlist.copy(trackList = trackList).toPlaylistJson().json
            }
        }
    }

    override suspend fun deletePlaylist(playlist: Playlist) {
        realm.write {
            val playlist = query<PlaylistJson>(query = "name = $0", playlist.name!!)
            delete(playlist)
        }
    }

    override suspend fun renamePlaylist(playlist: Playlist, name: String) {
        deletePlaylist(playlist)
        insertPlaylist(playlist.copy(name = name))
    }
}

class PlaylistJson(): RealmObject {
    @PrimaryKey
    var name: String = ""
    var json: String = ""

    fun toPlaylist(): Playlist = Json.decodeFromString(json)
}

fun Playlist.toPlaylistJson(): PlaylistJson = PlaylistJson().apply {
    this.name = this@toPlaylistJson.name!!
    json = Json.encodeToString(this@toPlaylistJson)
}