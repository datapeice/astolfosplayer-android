package com.datapeice.astolfosplayer.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE name = :name")
    suspend fun deleteByName(name: String)

    @Query("SELECT * FROM playlists WHERE name = :name")
    suspend fun getByName(name: String): PlaylistEntity?
}

@Dao
interface LyricsDao {
    @Query("SELECT * FROM lyrics WHERE uri = :uri")
    suspend fun getByUri(uri: String): LyricsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(lyrics: LyricsEntity)

    @Query("DELETE FROM lyrics WHERE uri = :uri")
    suspend fun deleteByUri(uri: String)
}