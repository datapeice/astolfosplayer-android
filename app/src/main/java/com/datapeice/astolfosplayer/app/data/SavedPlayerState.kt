package com.datapeice.astolfosplayer.app.data

import android.content.Context
import com.datapeice.astolfosplayer.app.domain.playback.PlaybackMode
import com.datapeice.astolfosplayer.app.domain.track.Playlist
import com.datapeice.astolfosplayer.app.domain.track.Track
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SavedPlayerState(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("saved-com.datapeice.astolfoplayer-state", Context.MODE_PRIVATE)

    private val playlistKey = "playlist"
    private val playbackModeKey = "playback-mode"
    private val trackKey = "track"

    var playlist: Playlist?
        get() = sharedPreferences.getString(playlistKey, null)?.let { Json.decodeFromString(it) }
        set(value) {
            with(sharedPreferences.edit()) {
                putString(playlistKey, Json.encodeToString(value))
                apply()
            }
        }

    var playbackMode: PlaybackMode
        get() = PlaybackMode.entries[sharedPreferences.getInt(playbackModeKey, 0)]
        set(value) {
            with(sharedPreferences.edit()) {
                putInt(playbackModeKey, value.ordinal)
                apply()
            }
        }

    var track: Track?
        get() = sharedPreferences.getString(trackKey, null)?.let { Json.decodeFromString(it) }
        set(value) {
            with(sharedPreferences.edit()) {
                putString(trackKey, Json.encodeToString(value))
                apply()
            }
        }
}