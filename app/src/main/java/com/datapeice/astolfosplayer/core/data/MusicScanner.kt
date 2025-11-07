package com.datapeice.astolfosplayer.core.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.media.MediaScannerConnection
import com.datapeice.astolfosplayer.R
import com.datapeice.astolfosplayer.app.presentation.components.snackbar.SnackbarAction
import com.datapeice.astolfosplayer.app.presentation.components.snackbar.SnackbarController
import com.datapeice.astolfosplayer.app.presentation.components.snackbar.SnackbarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MusicScanner(
    private val context: Context,
    private val settings: Settings
) {
    private val allowedExtensions = setOf("mp3", "wav", "aac", "flac", "ogg", "m4a")

    suspend fun refreshMedia(showMessages: Boolean = true, onComplete: () -> Unit = {}) {
        withContext(Dispatchers.IO) {
            try {
                // Получаем папку из настроек
                val selectedFolder = settings.extraScanFolders.value.firstOrNull()

                if (selectedFolder.isNullOrBlank()) {
                    if (showMessages) {
                        SnackbarController.sendEvent(
                            event = SnackbarEvent(
                                message = R.string.folder_not_selected // Добавьте эту строку в ресурсы
                            )
                        )
                    }
                    onComplete()
                    return@withContext
                }

                val directory = File(selectedFolder)

                if (!directory.exists() || !directory.isDirectory) {
                    if (showMessages) {
                        SnackbarController.sendEvent(
                            event = SnackbarEvent(
                                message = R.string.folder_not_found // Добавьте эту строку в ресурсы
                            )
                        )
                    }
                    onComplete()
                    return@withContext
                }

                // Сканируем ТОЛЬКО выбранную папку
                val paths = directory.walkTopDown()
                    .filter { it.isFile && it.extension.lowercase() in allowedExtensions }
                    .map { it.absolutePath }
                    .toList()
                    .toTypedArray()

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
                        paths,
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