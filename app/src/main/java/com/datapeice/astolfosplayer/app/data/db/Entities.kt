package com.datapeice.astolfosplayer.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val name: String,
    val json: String
)

@Entity(tableName = "lyrics")
data class LyricsEntity(
    @PrimaryKey val uri: String,
    val json: String
)