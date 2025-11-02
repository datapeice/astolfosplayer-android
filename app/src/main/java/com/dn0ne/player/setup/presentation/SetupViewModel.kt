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
    private val _login = MutableStateFlow(settings.login)
    val login = _login.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = settings.login
    )

    private val _password = MutableStateFlow(settings.password)
    val password = _password.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = settings.password
    )

    // Также сделаем StateFlow для адреса сервера, чтобы он тоже не пропадал при вводе
    private val _serverAddress = MutableStateFlow(settings.serverAddress)
    val serverAddress = _serverAddress.stateIn(
        scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000L),
        initialValue = settings.serverAddress
    )

    // --- ДОБАВЬТЕ ЭТИ ФУНКЦИИ-ОБРАБОТЧИКИ ---
    fun onServerAddressChanged(address: String) {
        _serverAddress.update { address }
    }

    fun onLoginChanged(login: String) {
        _login.update { login }}

    fun onPasswordChanged(password: String) {
        _password.update { password }
    }
    private val _serverUrlError = MutableStateFlow<String?>(null)

    val startDestination: SetupPage by mutableStateOf(
        if (!setupState.isComplete) {
            SetupPage.Welcome
        } else SetupPage.AudioPermission
    )
    val serverUrlError = _serverUrlError.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = null
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


    private fun isServerAddressValid(address: String): Boolean {
        // Regex: http:// + (цифры/точки для IP) + : + (цифры для порта)
        val urlRegex = Regex("""^http://(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}):(\d{1,5})$""")
        return urlRegex.matches(address)
    }

    // Обратите внимание, что я изменил возвращаемый тип функций на Boolean
    fun onLogin(serverAddress: String, login: String, password: String): Boolean {
        _serverUrlError.update { null }

        if (isServerAddressValid(serverAddress)) {
            // Адрес верный, сохраняем и пытаемся войти
            settings.serverAddress = serverAddress
            settings.login = login;
            settings.password = password;
            println("Login attempt with: server=$serverAddress, login=$login")
            // TODO: Реализовать логику входа (отправка запроса на сервер)
            return true // Возвращаем true, чтобы UI мог перейти на следующий экран
        } else {
            // Адрес неверный, сообщаем об ошибке
            _serverUrlError.update { "Неверный формат адреса. Пример: http://1.2.3.4:8000" }
            return false // Возвращаем false, чтобы UI НЕ переходил дальше
        }
    }

    fun onRegister(serverAddress: String, login: String, password: String): Boolean {
        // Сбрасываем предыдущую ошибку
        _serverUrlError.update { null }

        if (isServerAddressValid(serverAddress)) {
            // Адрес верный, сохраняем и пытаемся зарегистрироваться
            settings.serverAddress = serverAddress
            settings.login = login;
            settings.password = password;
            println("Register attempt with: server=$serverAddress, login=$login")
            // TODO: Реализовать логику регистрации
            return true
        } else {
            // Адрес неверный, сообщаем об ошибке
            _serverUrlError.update { "Неверный формат адреса. Пример: http://1.2.3.4:8000" }
            return false
        }
    }
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