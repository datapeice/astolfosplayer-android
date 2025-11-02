package com.dn0ne.player.setup.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dn0ne.player.core.data.MusicScanner
import com.dn0ne.player.core.data.Settings
import com.dn0ne.player.setup.data.SetupState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class SetupViewModel(
    private val setupState: SetupState,
    private val getFoldersWithAudio: () -> Set<String>,
    val settings: Settings,
    val musicScanner: MusicScanner
) : ViewModel() {
    val startDestination: SetupPage by mutableStateOf(
        if (!setupState.isComplete) {
            SetupPage.Welcome
        } else SetupPage.AudioPermission
    )

    private val _isAudioPermissionGranted = MutableStateFlow(false)
    val isAudioPermissionGranted = _isAudioPermissionGranted.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = false
    )

    val isSetupComplete: Boolean
        get() = setupState.isComplete

    private val _foldersWithAudio = MutableStateFlow(emptySet<String>())
    val foldersWithAudio = _foldersWithAudio.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptySet()
    )
// Вставьте этот код внутрь класса SetupViewModel

// --- НАЧАЛО БЛОКА ДЛЯ КОПИРОВАНИЯ ---

    fun onLogin(serverAddress: String, login: String, password: String) {
        // TODO: Реализовать логику входа
        // 1. Сохранить адрес сервера
        settings.serverAddress = serverAddress
        // 2. Отправить запрос на сервер для аутентификации
        // 3. В случае успеха сохранить токен/сессию
        // 4. В случае ошибки показать сообщение пользователю (через State/Flow)
        println("Login attempt with: server=$serverAddress, login=$login")
    }

    fun onRegister(serverAddress: String, login: String, password: String) {
        // TODO: Реализовать логику регистрации
        // 1. Сохранить адрес сервера
        settings.serverAddress = serverAddress
        // 2. Отправить запрос на сервер для создания нового пользователя
        // 3. Обработать ответ (успех/ошибка)
        // 4. Возможно, автоматически войти в систему после успешной регистрации
        println("Register attempt with: server=$serverAddress, login=$login")
    }

// --- КОНЕЦ БЛОКА ---

    fun onScanFoldersClick() {
        _foldersWithAudio.update {
            getFoldersWithAudio()
        }
    }

    fun onFinishSetupClick() {
        setupState.isComplete = true
    }

    fun onAudioPermissionRequest(isGranted: Boolean) {
        _isAudioPermissionGranted.update {
            isGranted
        }
    }

    fun onFolderPicked(path: String) {
        if (settings.isScanModeInclusive.value) {
            settings.updateExtraScanFolders(settings.extraScanFolders.value + path)
        } else {
            settings.updateExcludedScanFolders(settings.extraScanFolders.value + path)
        }
    }
}