package com.dn0ne.player.setup.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

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
    onLoginClick: (String, String, String) -> Unit,
    onRegisterClick: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Состояния для хранения значений текстовых полей
    var serverAddress by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Проверяем, все ли поля заполнены, чтобы активировать кнопки
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
            Text(
                text = "Настройка сервера", // Замените на stringResource
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Поле для адреса сервера
            OutlinedTextField(
                value = serverAddress,
                onValueChange = { serverAddress = it },
                label = { Text("Адрес сервера (например, 192.168.1.100)") }, // Замените на stringResource
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле для логина
            OutlinedTextField(
                value = login,
                onValueChange = { login = it },
                label = { Text("Логин") }, // Замените на stringResource
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле для пароля
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") }, // Замените на stringResource
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(), // Скрывает символы пароля
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
        }

        // Ряд с кнопками внизу экрана
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter) // Размещаем по центру внизу
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Кнопка "Регистрация"
            TextButton(
                onClick = { onRegisterClick(serverAddress, login, password) },
                enabled = areFieldsFilled // Кнопка активна, если все поля заполнены
            ) {
                Text("Регистрация") // Замените на stringResource
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Кнопка "Логин" (основная)
            Button(
                onClick = { onLoginClick(serverAddress, login, password) },
                enabled = areFieldsFilled // Кнопка активна, если все поля заполнены
            ) {
                Text("Логин") // Замените на stringResource
            }
        }
    }
}
