package com.datapeice.astolfoplayer.app.data.repository

import com.datapeice.astolfoplayer.app.domain.track.Track

interface TrackRepository {
    fun getTracks(): List<Track>
    fun getFoldersWithAudio(): Set<String>
}