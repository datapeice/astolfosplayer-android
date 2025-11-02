package com.datapeice.astolfoplayer.app.data.remote.metadata

import com.datapeice.astolfoplayer.app.domain.metadata.MetadataSearchResult
import com.datapeice.astolfoplayer.app.domain.result.DataError
import com.datapeice.astolfoplayer.app.domain.result.Result

interface MetadataProvider {
    suspend fun searchMetadata(query: String, trackDuration: Long): Result<List<MetadataSearchResult>, DataError>
    suspend fun getCoverArtBytes(searchResult: MetadataSearchResult): Result<ByteArray, DataError>
}