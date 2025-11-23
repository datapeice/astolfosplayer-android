package com.datapeice.astolfosplayer.app.data.repository

import com.datapeice.astolfosplayer.app.data.db.LyricsDao
import com.datapeice.astolfosplayer.app.data.db.LyricsEntity
import com.datapeice.astolfosplayer.app.domain.lyrics.Lyrics
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RoomLyricsRepository(
    private val dao: LyricsDao
) : LyricsRepository {

    // Теперь это suspend функция, всё чисто и асинхронно
    override suspend fun getLyricsByUri(uri: String): Lyrics? {
        val entity = dao.getByUri(uri)
        return entity?.let { Json.decodeFromString<Lyrics>(it.json) }
    }

    override suspend fun insertLyrics(lyrics: Lyrics) {
        dao.insertOrUpdate(lyrics.toEntity())
    }

    override suspend fun updateLyrics(lyrics: Lyrics) {
        dao.insertOrUpdate(lyrics.toEntity())
    }

    override suspend fun deleteLyrics(lyrics: Lyrics) {
        dao.deleteByUri(lyrics.uri)
    }

    private fun Lyrics.toEntity(): LyricsEntity {
        return LyricsEntity(
            uri = this.uri,
            json = Json.encodeToString(this)
        )
    }
}