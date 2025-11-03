package com.datapeice.astolfosplayer.app.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Composable, который отображает тонкую полосу прогресса синхронизации.
 * @param progress Прогресс от 0.0f до 1.0f.
 * @param message Текущее сообщение о статусе синхронизации.
 * @param modifier Модификатор.
 */
@Composable
fun SyncProgressBar(
    progress: Float,
    message: String,
    modifier: Modifier = Modifier
) {
    // Анимируем изменение прогресса для плавности
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "SyncProgressAnimation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp) // Стандартная высота
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Задний фон прогресс-бара
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            trackColor = Color.Transparent
        )

        // Текст с текущим статусом
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
