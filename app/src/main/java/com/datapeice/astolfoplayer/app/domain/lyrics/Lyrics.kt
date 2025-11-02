package com.datapeice.astolfoplayer.app.domain.lyrics

import kotlinx.serialization.Serializable

@Serializable
data class Lyrics(
    val uri: String,
    val areFromRemote: Boolean = true,
    val plain: List<String>? = null,
    val synced: List<Pair<Int, String>>? = null
)

fun String.toSyncedLyrics(): List<Pair<Int, String>> {
    val regex = """\[(\d+):(\d+)\.(\d+)].*""".toRegex()
    return split('\n')
        .filter {
            it.matches(regex)
        }
        .ifEmpty { throw IllegalArgumentException("Synced lines not found.") }
        .map {
            val timestampString = it.substring(1..8)
            val timestamp = timestampString.toLyricsTimestamp()

            timestamp to it.drop(10).trim()
        }
}

fun String.toLyricsTimestamp(): Int {
    val regex = Regex("""(\d+):(\d+)\.(\d+)""")
    regex.matchEntire(this)?.let { matchResult ->
        val minutes = matchResult.groupValues.getOrNull(1)?.toIntOrNull()
        val seconds = matchResult.groupValues.getOrNull(2)?.toIntOrNull()
        val centiseconds = matchResult.groupValues.getOrNull(3)?.toIntOrNull()

        if (minutes == null || seconds == null || centiseconds == null) {
            throw IllegalArgumentException("Failed to parse timestamp: $this")
        }

        return minutes * 60 * 1000 + seconds * 1000 + centiseconds * 10
    }
        ?: throw IllegalArgumentException("Failed to parse timestamp, does not match regex ${regex.pattern}")
}
