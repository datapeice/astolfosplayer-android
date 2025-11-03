package com.datapeice.astolfosplayer.setup.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.datapeice.astolfosplayer.setup.presentation.LoadingState

@Composable
fun LoadingDialog(
    state: LoadingState,
    onDismiss: () -> Unit
) {
    if (!state.isLoading && !state.isFinished) {
        return // Не показывать диалог, если нет ни загрузки, ни результата
    }

    Dialog(onDismissRequest = { /* Не позволяем закрыть диалог свайпом или кнопкой "назад" */ }) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Показываем кружок загрузки только во время процесса
                if (state.isLoading) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Кнопка "ОК"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onDismiss,
                    // Кнопка активна только после завершения запроса
                    enabled = state.isFinished
                ) {
                    Text("ОК")
                }
            }
        }
    }
}
