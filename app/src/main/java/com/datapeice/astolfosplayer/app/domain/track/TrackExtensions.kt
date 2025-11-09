package com.datapeice.astolfosplayer.app.domain.track

import android.net.Uri
import android.util.Log

/**
 * Фильтрует треки по выбранной папке.
 * ВАЖНО: Если папка не выбрана - возвращает ПУСТОЙ список!
 * Пользователь ОБЯЗАН выбрать папку для работы приложения.
 */
fun List<Track>.filterBySelectedFolder(folderUri: String?): List<Track> {
    // Папка не выбрана = ПУСТОЙ СПИСОК
    if (folderUri.isNullOrBlank()) {
        Log.w("TrackFilter", "⚠️ Папка не выбрана! Список треков пуст.")
        return emptyList()
    }

    // Преобразуем URI папки в реальный путь файловой системы
    val folderPath = try {
        val decoded = Uri.decode(folderUri)
        when {
            // content://...tree/primary:Music → /storage/emulated/0/Music
            decoded.contains("primary:") -> {
                val path = decoded.substringAfter("primary:")
                    .substringBefore("/document")
                "/storage/emulated/0/$path"
            }
            // content://...tree/1234-5678:Music → /storage/1234-5678/Music (SD-карта)
            decoded.contains(":") -> {
                val parts = decoded.substringAfter("tree/")
                    .substringBefore("/document")
                    .split(":")
                if (parts.size >= 2) {
                    "/storage/${parts[0]}/${parts[1]}"
                } else null
            }
            else -> null
        }
    } catch (e: Exception) {
        Log.e("TrackFilter", "❌ Ошибка парсинга URI: $folderUri", e)
        null
    }

    // Не удалось преобразовать = ПУСТОЙ СПИСОК
    if (folderPath == null) {
        Log.e("TrackFilter", "❌ Не удалось преобразовать URI в путь: $folderUri")
        return emptyList()
    }

    Log.d("TrackFilter", "📁 Фильтрация по папке: $folderPath")

    // Оставляем только треки, которые находятся в выбранной папке
    val filtered = this.filter { track ->
        track.data.startsWith(folderPath)
    }

    Log.d("TrackFilter", "✅ Найдено ${filtered.size} треков из ${this.size} в базе")

    if (filtered.isEmpty()) {
        Log.w("TrackFilter", "⚠️ В папке $folderPath нет треков!")
    }

    return filtered
}