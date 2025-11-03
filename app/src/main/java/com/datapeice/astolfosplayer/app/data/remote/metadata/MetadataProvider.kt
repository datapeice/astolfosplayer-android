package com.datapeice.astolfosplayer.app.data.remote.metadata

import com.datapeice.astolfosplayer.app.domain.metadata.MetadataSearchResult
import com.datapeice.astolfosplayer.app.domain.result.DataError
import com.datapeice.astolfosplayer.app.domain.result.Result

interface MetadataProvider {
    suspend fun searchMetadata(query: String, trackDuration: Long): Result<List<MetadataSearchResult>, DataError>
    suspend fun getCoverArtBytes(searchResult: MetadataSearchResult): Result<ByteArray, DataError>
}