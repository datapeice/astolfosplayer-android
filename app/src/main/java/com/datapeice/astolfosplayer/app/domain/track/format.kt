package com.datapeice.astolfosplayer.app.domain.track

val Track.format: String
    get() = data.substringAfterLast(".")