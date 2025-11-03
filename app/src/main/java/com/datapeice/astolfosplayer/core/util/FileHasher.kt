package com.datapeice.astolfosplayer.core.utils

import java.io.InputStream
import java.security.MessageDigest

object FileHasher {
    fun calculateSha256(inputStream: InputStream): String? {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            inputStream.close()
        }
    }
}
