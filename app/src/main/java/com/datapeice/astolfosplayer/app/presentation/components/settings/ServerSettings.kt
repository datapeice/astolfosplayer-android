package com.datapeice.astolfosplayer.app.presentation.components.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.window.Dialog
import com.datapeice.astolfosplayer.app.presentation.components.topbar.ColumnWithCollapsibleTopBar
import com.datapeice.astolfosplayer.core.data.Settings
import com.datapeice.astolfosplayer.setup.presentation.SetupViewModel
import com.datapeice.astolfosplayer.setup.presentation.components.LoadingDialog
import com.datapeice.astolfosplayer.R

/**import com.datapeice.astolfosplayer.R

 * Главный Composable для экрана настроек сервера.
 * Теперь он содержит логику для отображения и редактирования.
 */
@Composable
fun ServerSettings(
    settings: Settings,
    setupViewModel: SetupViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Состояние, которое определяет, открыт ли диалог редактирования
    var isEditing by remember { mutableStateOf(false) }

    val loadingState by setupViewModel.loadingState.collectAsState()

    // Основной UI экрана с заголовком
    ColumnWithCollapsibleTopBar(
        topBarContent = {
            // ... (Код заголовка остается без изменений)
            IconButton(onClick = onBackClick, modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = 12.dp, vertical = 4.dp)) {
                Icon(imageVector = Icons.Rounded.ArrowBackIosNew, contentDescription = stringResource(R.string.back))
            }
            var collapseFraction by remember { mutableFloatStateOf(0f) }
            Text(
                text = stringResource(R.string.server_setting),
                fontSize = lerp(MaterialTheme.typography.titleLarge.fontSize, MaterialTheme.typography.displaySmall.fontSize, collapseFraction),
                textAlign = TextAlign.Center,
                modifier = Modifier.align(Alignment.Center).padding(horizontal = 16.dp)
            )
        },
        contentPadding = PaddingValues(16.dp),
        modifier = modifier
    ) {
        // Компонент, который показывает текущие настройки
        CurrentSettingsContent(
            settings = settings,
            onEditClick = { isEditing = true } // По клику на карандаш открываем диалог
        )
    }

    // Показываем диалог редактирования, если isEditing = true
    if (isEditing) {
        EditServerSettingsDialog(
            setupViewModel = setupViewModel,
            onDismiss = { isEditing = false } // Закрываем диалог
        )
    }

    // Диалог загрузки при проверке соединения
    LoadingDialog(
        state = loadingState,
        onDismiss = { setupViewModel.dismissLoadingDialog() }
    )
}

/**
 * Компонент для отображения ТЕКУЩИХ настроек в виде текста.
 */
@Composable
private fun CurrentSettingsContent(
    settings: Settings,
    onEditClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Блок с заголовком и кнопкой редактирования
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.current_server_settings),
                style = MaterialTheme.typography.titleLarge
            )
            IconButton(onClick = onEditClick) {
                Icon(imageVector = Icons.Rounded.Edit, contentDescription = stringResource(R.string.edit))
            }
        }

        // Отображение данных
        SettingInfoRow(title = stringResource(R.string.access_token), value = if (settings.accessToken.isBlank()) stringResource(R.string.token_not_received) else settings.accessToken)
        Divider()
        SettingInfoRow(title = stringResource(R.string.server_address), value = settings.serverAddress.ifBlank { stringResource(R.string.no_data) })
        Divider()
        SettingInfoRow(title = stringResource(R.string.login), value = settings.login.ifBlank { stringResource(R.string.no_data) })
        Divider()
        SettingInfoRow(title = stringResource(R.string.password), value = if (settings.password.isBlank()) stringResource(R.string.no_data) else stringResource(R.string.password_hidden))
    }
}

/**
 * Вспомогательный компонент для красивого отображения строки "Заголовок: Значение"
 */
@Composable
private fun SettingInfoRow(title: String, value: String) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 4.dp))
    }
}

/**
 * Диалоговое окно для РЕДАКТИРОВАНИЯ настроек.
 */
@Composable
private fun EditServerSettingsDialog(
    setupViewModel: SetupViewModel,
    onDismiss: () -> Unit
) {
    // Подписываемся на состояния из ViewModel
    val serverAddress by setupViewModel.serverAddress.collectAsState()
    val login by setupViewModel.login.collectAsState()
    val password by setupViewModel.password.collectAsState()
    val serverErrorResId by setupViewModel.serverUrlError.collectAsState()
    val loginSuccessful by setupViewModel.loginSuccessful.collectAsState()

    // Если логин прошел успешно, закрываем диалог
    LaunchedEffect(loginSuccessful) {
        if (loginSuccessful) {
            setupViewModel.onNavigationHandled() // Сбрасываем флаг
            onDismiss()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = stringResource(R.string.server_setting), style = MaterialTheme.typography.headlineSmall)

                OutlinedTextField(
                    value = serverAddress,
                    onValueChange = setupViewModel::onServerAddressChanged,
                    label = { Text(stringResource(R.string.server_address_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    isError = serverErrorResId != null
                )

                if (serverErrorResId != null) {
                    Text(
                        text = stringResource(id = serverErrorResId),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                OutlinedTextField(
                    value = login,
                    onValueChange = setupViewModel::onLoginChanged,
                    label = { Text(stringResource(R.string.login)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = setupViewModel::onPasswordChanged,
                    label = { Text(stringResource(R.string.password)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { setupViewModel.onLogin() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}

private fun ColumnScope.stringResource(id: String?): String {
    return TODO("Provide the return value")
}
