package com.datapeice.astolfosplayer.app.data

import android.content.Context
import com.datapeice.astolfosplayer.app.domain.lyrics.Lyrics
import com.datapeice.astolfosplayer.app.domain.result.DataError
import com.datapeice.astolfosplayer.app.domain.result.Result
import com.datapeice.astolfosplayer.app.domain.track.Track
import com.datapeice.astolfosplayer.app.domain.track.format
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File
import java.io.FileOutputStream

class LyricsReaderImpl(private val context: Context) : LyricsReader {
    override fun readFromTag(track: Track): Result<Lyrics?, DataError.Local> {
        var lyrics: Lyrics? = null

        var file: File? = null
        context.contentResolver.openInputStream(track.uri)?.use { input ->
            val temp = File.createTempFile("temp_audio", ".${track.format}", context.cacheDir)
            FileOutputStream(temp).use { output ->
                input.copyTo(output)
            }
            file = temp
        }

        if (file == null) return Result.Error(DataError.Local.NoReadPermission)

        val audioFile =
            AudioFileIO.read(file) ?: return Result.Error(DataError.Local.FailedToRead)
        val tag = audioFile.tagAndConvertOrCreateAndSetDefault
            ?: return Result.Error(DataError.Local.FailedToRead)

        var lyricsText = tag.getFirst(FieldKey.LYRICS)

        if (lyricsText?.isNotBlank() == true) {
            lyrics = Lyrics(
                uri = track.uri.toString(),
                plain = lyricsText.split('\n'),
                synced = null,
                areFromRemote = false
            )
        }

        context.cacheDir?.deleteRecursively()
        return Result.Success(lyrics)
    }
}