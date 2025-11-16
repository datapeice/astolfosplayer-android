package com.datapeice.astolfosplayer.app.data.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class TrackIdStorage(private val context: Context) {
    private val prefs = context.getSharedPreferences("track_ids", Context.MODE_PRIVATE)

    suspend fun saveTrackId(fileHash: String, trackId: String) = withContext(Dispatchers.IO) {
        prefs.edit().putString(fileHash, trackId).apply()
        Log.d("TrackIdStorage", "Saved: hash=$fileHash -> id=$trackId")
    }

    fun getTrackId(fileHash: String): String? {
        return prefs.getString(fileHash, null)
    }

    suspend fun removeTrackId(fileHash: String) = withContext(Dispatchers.IO) {
        prefs.edit().remove(fileHash).apply()
        Log.d("TrackIdStorage", "Removed: hash=$fileHash")
    }

    fun getAllMappings(): Map<String, String> {
        return prefs.all.mapNotNull { (key, value) ->
            if (value is String) key to value else null
        }.toMap()
    }
}