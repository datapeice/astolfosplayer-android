package com.datapeice.astolfosplayer.setup.presentation.components

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.datapeice.astolfosplayer.R
import java.net.URLDecoder

@Composable
fun MusicScanPage(
    selectedFolderUriString: String?, // <-- Теперь это строка с URI
    onPickFolderClick: () -> Unit,
    onFinishClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(28.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            SetupPageHeader(
                title = "Выбор папки", // TODO: stringResource
                icon = Icons.Rounded.Folder
            )
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Пожалуйста, выберите основную папку с вашей музыкой.", // TODO: stringResource
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Показываем карточку с выбранной папкой или кнопку "Выбрать"
            if (selectedFolderUriString != null) {
                // Вызываем наш новый FolderItem, который умеет работать с URI
                FolderItem(uriString = selectedFolderUriString)
            } else {
                Button(onClick = onPickFolderClick) {
                    Text(text = "Выбрать папку") // TODO: stringResource
                }
            }
        }

        // Кнопка "Next" всегда внизу и активна, только если папка выбрана
        Button(
            onClick = onFinishClick,
            enabled = selectedFolderUriString != null,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.next))
        }
    }
}

@Composable
private fun FolderItem(uriString: String) {
    // Получаем контекст, он нужен для нашей функции
    val context = LocalContext.current
    val readablePath = remember(uriString) {
        // Преобразуем URI в читаемый путь прямо здесь
        getReadablePathFromUri(context, uriString)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(imageVector = Icons.Rounded.Folder, contentDescription = null)
            // Отображаем уже красивый путь
            Text(text = readablePath, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/**
 * Вспомогательная функция для преобразования URI в читаемый путь.
 * Находится здесь, так как это логика отображения.
 */
private fun getReadablePathFromUri(context: Context, uriString: String): String {
    return try {
        val uri = Uri.parse(uriString)
        // Простой и надежный способ для большинства случаев
        val decodedPath = URLDecoder.decode(uri.path, "UTF-8")
        val finalPath = decodedPath.substringAfterLast(':').ifEmpty { decodedPath }

        // Улучшаем отображение для известных типов
        when {
            uri.toString().contains("primary") -> "Внутренняя память / $finalPath"
            else -> finalPath
        }
    } catch (e: Exception) {
        // В случае ошибки возвращаем что-то осмысленное, а не крэш
        uriString.substringAfterLast('%') // Простая попытка показать хоть что-то
    }
}