package com.datapeice.astolfosplayer.app.domain.track

import android.net.Uri
import androidx.media3.common.MediaItem
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

object TrackSerializer : KSerializer<Track> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Track") {
        element<String?>("id")
        element<String>("uri")
        element<String>("data")
        element<String?>("title")
        element<String?>("artist")
        element<String?>("album")
        element<String?>("albumArtist")
        element<String?>("genre")
        element<String?>("year")
        element<String?>("trackNumber")
        element<String?>("bitrate")
        element<Int>("duration")
        element<Long>("size")
        element<Long>("dateModified")
        element<String>("coverArtUri")
    }

    override fun serialize(encoder: Encoder, value: Track) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.id ?: "")
            encodeStringElement(descriptor, 1, value.uri.toString())
            encodeStringElement(descriptor, 2, value.data)
            encodeStringElement(descriptor, 3, value.title ?: "null")
            encodeStringElement(descriptor, 4, value.artist ?: "null")
            encodeStringElement(descriptor, 5, value.album ?: "null")
            encodeStringElement(descriptor, 6, value.albumArtist ?: "null")
            encodeStringElement(descriptor, 7, value.genre ?: "null")
            encodeStringElement(descriptor, 8, value.year ?: "null")
            encodeStringElement(descriptor, 9, value.trackNumber ?: "null")
            encodeStringElement(descriptor, 10, value.bitrate ?: "null")
            encodeIntElement(descriptor, 11, value.duration)
            encodeLongElement(descriptor, 12, value.size)
            encodeLongElement(descriptor, 13, value.dateModified)
            encodeStringElement(descriptor, 14, value.coverArtUri.toString())
        }
    }

    override fun deserialize(decoder: Decoder): Track = decoder.decodeStructure(descriptor) {
        var id: String? = null
        var uriString = ""
        var data = ""
        var title: String? = null
        var artist: String? = null
        var album: String? = null
        var albumArtist: String? = null
        var genre: String? = null
        var year: String? = null
        var trackNumber: String? = null
        var bitrate: String? = null
        var duration = -1
        var size = -1L
        var dateModified = -1L
        var coverArtUriString = ""

        loop@ while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> id = decodeStringElement(descriptor, 0).takeIf { it.isNotBlank() }
                1 -> uriString = decodeStringElement(descriptor, 1)
                2 -> data = decodeStringElement(descriptor, 2)
                3 -> title = decodeStringElement(descriptor, 3).takeIf { it != "null" }
                4 -> artist = decodeStringElement(descriptor, 4).takeIf { it != "null" }
                5 -> album = decodeStringElement(descriptor, 5).takeIf { it != "null" }
                6 -> albumArtist = decodeStringElement(descriptor, 6).takeIf { it != "null" }
                7 -> genre = decodeStringElement(descriptor, 7).takeIf { it != "null" }
                8 -> year = decodeStringElement(descriptor, 8).takeIf { it != "null" }
                9 -> trackNumber = decodeStringElement(descriptor, 9).takeIf { it != "null" }
                10 -> bitrate = decodeStringElement(descriptor, 10).takeIf { it != "null" }
                11 -> duration = decodeIntElement(descriptor, 11)
                12 -> size = decodeLongElement(descriptor, 12)
                13 -> dateModified = decodeLongElement(descriptor, 13)
                14 -> coverArtUriString = decodeStringElement(descriptor, 14)
                CompositeDecoder.DECODE_DONE -> break@loop
                else -> error("Unexpected index: $index")
            }
        }

        val uri = Uri.parse(uriString)
        val mediaItem = MediaItem.fromUri(uri)
        val coverArtUri = Uri.parse(coverArtUriString)

        Track(
            id = id,
            uri = uri,
            mediaItem = mediaItem,
            coverArtUri = coverArtUri,
            duration = duration,
            size = size,
            dateModified = dateModified,
            data = data,
            title = title,
            artist = artist,
            album = album,
            albumArtist = albumArtist,
            genre = genre,
            year = year,
            trackNumber = trackNumber,
            bitrate = bitrate
        )
    }
}
