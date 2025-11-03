package com.datapeice.astolfosplayer.setup.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.datapeice.astolfosplayer.R
import androidx.compose.material.icons.Icons // <-- Добавьте этот импорт
import androidx.compose.material.icons.filled.Dns // <-- И этот тоже

/**
 * Экран для настройки сервера, входа в систему или регистрации.
 *
 * @param onLoginClick Колбэк, вызываемый при нажатии на кнопку "Логин".
 *                     Передает адрес сервера, логин и пароль.
 * @param onRegisterClick Колбэк, вызываемый при нажатии на кнопку "Регистрация".
 *                        Передает адрес сервера, логин и пароль.
 * @param modifier Модификатор для этого composable.
 */
@Composable
fun ServerSetupPage(
    // Изменяем колбэки, чтобы они соответствовали новым кнопкам
    serverAddress: String,login: String,
    password: String,
    onServerAddressChange: (String) -> Unit,
    onLoginChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    errorMessage: String?, // <-- ДОБАВЬТЕ ЭТОТ ПАРАМЕТР
    modifier: Modifier = Modifier
) {
    val areFieldsFilled = serverAddress.isNotBlank() && login.isNotBlank() && password.isNotBlank()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(28.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Заголовок страницы
            SetupPageHeader(
                title = stringResource(R.string.server_setting),
                icon = Icons.Filled.Dns // Используем иконку сервера
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Поле для адреса сервера
            OutlinedTextField(
                value = serverAddress,
                onValueChange = onServerAddressChange,
                label = { Text(stringResource(R.string.server_address_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                isError = errorMessage != null

            )

            Spacer(modifier = Modifier.height(16.dp))
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
            // Поле для логина
            OutlinedTextField(
                value = login,
                onValueChange = onLoginChange,
                label = { Text(stringResource(R.string.login)) }, // Замените на stringResource
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле для пароля
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(R.string.password)) }, // Замените на stringResource
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(), // Скрывает символы пароля
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
        }


        // Ряд с кнопками внизу экрана
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd) // Выравниваем весь ряд по нижнему правому краю
                .fillMaxWidth(), // Растягиваем на всю ширину
            horizontalArrangement = Arrangement.SpaceBetween, // <<< КЛЮЧЕВОЕ ИЗМЕНЕНИЕ: расталкивает элементы по краям
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Кнопка "Регистрация" слева
            Button( // <<< ИЗМЕНЕНО: теперь это тоже Button
                onClick = onRegisterClick,
                enabled = areFieldsFilled
            ) {
                Text(stringResource(R.string.register)) // Замените на stringResource
            }

            // Кнопка "Логин" справа
            Button(
                onClick = onLoginClick,
                enabled = areFieldsFilled
            ) {
                Text(stringResource(R.string.login)) // Замените на stringResource
            }
        }


    }
}
