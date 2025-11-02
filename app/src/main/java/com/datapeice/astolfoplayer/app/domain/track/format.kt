package com.datapeice.astolfoplayer.app.domain.track

val Track.format: String
    get() = data.substringAfterLast(".")