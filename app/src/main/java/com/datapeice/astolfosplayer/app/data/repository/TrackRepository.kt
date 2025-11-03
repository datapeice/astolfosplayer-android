package com.datapeice.astolfosplayer.app.data.repository

import com.datapeice.astolfosplayer.app.domain.track.Track

interface TrackRepository {
    fun getTracks(): List<Track>
    fun getFoldersWithAudio(): Set<String>
}