package com.datapeice.astolfoplayer.app.presentation.components.playback

import com.datapeice.astolfoplayer.app.domain.lyrics.Lyrics
import com.datapeice.astolfoplayer.app.domain.playback.PlaybackMode
import com.datapeice.astolfoplayer.app.domain.track.Playlist
import com.datapeice.astolfoplayer.app.domain.track.Track

data class PlaybackState(
    val playlist: Playlist? = null,
    val currentTrack: Track? = null,
    val lyrics: Lyrics? = null,
    val isLoadingLyrics: Boolean = false,
    val isPlaying: Boolean = false,
    val playbackMode: PlaybackMode = PlaybackMode.Repeat,
    val position: Long = 0L,

    val isPlayerExpanded: Boolean = false,
    val isLyricsSheetExpanded: Boolean = false,
)