package com.datapeice.astolfosplayer.core.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.datapeice.astolfosplayer.R
import com.datapeice.astolfosplayer.app.presentation.components.snackbar.SnackbarAction
import com.datapeice.astolfosplayer.app.presentation.components.snackbar.SnackbarController
import com.datapeice.astolfosplayer.app.presentation.components.snackbar.SnackbarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MusicScanner(
    private val context: Context,
    private val settings: Settings
) {
    private val allowedExtensions = setOf("mp3", "wav", "aac", "flac", "ogg", "m4a")

    suspend fun refreshMedia(showMessages: Boolean = true, onComplete: () -> Unit = {}) {
        withContext(Dispatchers.IO) {
            try {
                // Получаем папку из настроек (это URI-строка)
                val selectedFolderUri = settings.extraScanFolders.value.firstOrNull()

                if (selectedFolderUri.isNullOrBlank()) {
                    if (showMessages) {
                        SnackbarController.sendEvent(
                            event = SnackbarEvent(
                                message = R.string.folder_not_selected
                            )
                        )
                    }
                    onComplete()
                    return@withContext
                }

                // Используем DocumentFile для работы с URI
                val directory = DocumentFile.fromTreeUri(context, Uri.parse(selectedFolderUri))

                if (directory == null || !directory.exists() || !directory.isDirectory) {
                    if (showMessages) {
                        SnackbarController.sendEvent(
                            event = SnackbarEvent(
                                message = R.string.folder_not_found
                            )
                        )
                    }
                    onComplete()
                    return@withContext
                }

                // Рекурсивно собираем все файлы
                val paths = mutableListOf<String>()
                collectAudioFiles(directory, paths)

                if (paths.isEmpty()) {
                    if (showMessages) {
                        SnackbarController.sendEvent(
                            event = SnackbarEvent(
                                message = R.string.nothing_to_refresh
                            )
                        )
                    }
                } else {
                    MediaScannerConnection.scanFile(
                        context,
                        paths.toTypedArray(),
                        arrayOf("audio/*"),
                        null
                    )

                    if (showMessages) {
                        SnackbarController.sendEvent(
                            event = SnackbarEvent(
                                message = R.string.refreshed_successfully
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                if (!showMessages) return@withContext
                SnackbarController.sendEvent(
                    SnackbarEvent(
                        message = R.string.failed_to_refresh,
                        action = SnackbarAction(
                            name = R.string.copy_error,
                            action = {
                                val clipboardManager =
                                    context.getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager
                                val clip =
                                    ClipData.newPlainText(
                                        null,
                                        e.message + "\n" + e.stackTrace.joinToString("\n")
                                    )
                                clipboardManager?.setPrimaryClip(clip)
                            }
                        )
                    )
                )
            }
            onComplete()
        }
    }

    // Рекурсивно собираем пути к аудиофайлам
    private fun collectAudioFiles(directory: DocumentFile, paths: MutableList<String>) {
        directory.listFiles().forEach { file ->
            if (file.isDirectory) {
                // Рекурсивно обходим поддиректории
                collectAudioFiles(file, paths)
            } else if (file.isFile) {
                val name = file.name ?: return@forEach
                val extension = name.substringAfterLast('.', "").lowercase()
                if (extension in allowedExtensions) {
                    // Преобразуем URI в путь (если возможно) или используем URI напрямую
                    val path = file.uri.toString()
                    paths.add(path)
                }
            }
        }
    }

    suspend fun scanFolder(path: String, onComplete: () -> Unit = {}) {
        withContext(Dispatchers.IO) {
            try {
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(path),
                    arrayOf("audio/*"),
                    null
                )

                SnackbarController.sendEvent(
                    event = SnackbarEvent(
                        message = R.string.scanned_successfully
                    )
                )
            } catch (e: Exception) {
                SnackbarController.sendEvent(
                    SnackbarEvent(
                        message = R.string.failed_to_scan,
                        action = SnackbarAction(
                            name = R.string.copy_error,
                            action = {
                                val clipboardManager =
                                    context.getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager
                                val clip =
                                    ClipData.newPlainText(
                                        null,
                                        e.message + "\n" + e.stackTrace.joinToString("\n")
                                    )
                                clipboardManager?.setPrimaryClip(clip)
                            }
                        )
                    )
                )
            }
            onComplete()
        }
    }
}