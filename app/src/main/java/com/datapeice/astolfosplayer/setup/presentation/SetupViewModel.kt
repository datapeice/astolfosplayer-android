package com.datapeice.astolfosplayer.setup.presentation

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datapeice.astolfosplayer.R
import com.datapeice.astolfosplayer.core.api.AuthApi
import com.datapeice.astolfosplayer.core.api.AuthRequest
import com.datapeice.astolfosplayer.core.api.AuthResponse
import com.datapeice.astolfosplayer.core.data.MusicScanner
import com.datapeice.astolfosplayer.core.data.Settings
import com.datapeice.astolfosplayer.setup.data.SetupState
import io.ktor.client.call.body
import io.ktor.client.plugins.ConnectTimeoutException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException


data class LoadingState(
    val isLoading: Boolean = false,
    val isFinished: Boolean = false,
    val message: String = ""
)

// --- ViewModel ---

class SetupViewModel(
    private val setupState: SetupState,
    private val getFoldersWithAudio: () -> Set<String>,
    val settings: Settings,
    val musicScanner: MusicScanner,
    private val authApi: AuthApi,
    private val context: Context
) : ViewModel() {

    // --- Состояния для UI ---

    val startDestination: SetupPage by mutableStateOf(
        if (!setupState.isComplete) SetupPage.Welcome else SetupPage.AudioPermission
    )

    // Состояния для полей ввода
    private val _serverAddress = MutableStateFlow(settings.serverAddress)
    val serverAddress = _serverAddress.asStateFlow()

    private val _login = MutableStateFlow(settings.login)
    val login = _login.asStateFlow()

    private val _password = MutableStateFlow(settings.password)
    val password = _password.asStateFlow()

    // Состояние для ошибок
    private val _serverUrlError = MutableStateFlow<String?>(null)
    val serverUrlError = _serverUrlError.asStateFlow()

    // Состояние для диалога загрузки
    private val _loadingState = MutableStateFlow(LoadingState())
    val loadingState = _loadingState.asStateFlow()

    // Состояние для навигации после успешного входа
    private val _loginSuccessful = MutableStateFlow(false)
    val loginSuccessful = _loginSuccessful.asStateFlow()

    // Состояния для других экранов настройки
    private val _isAudioPermissionGranted = MutableStateFlow(false)
    val isAudioPermissionGranted = _isAudioPermissionGranted.asStateFlow()

    private val _selectedFolder = MutableStateFlow<String?>(null)
    val selectedFolder = _selectedFolder.asStateFlow()

    val isSetupComplete: Boolean
        get() = setupState.isComplete


    // --- Обработчики событий от UI ---

    private fun handleNetworkException(e: Exception) {
        e.printStackTrace()
        val errorMessage = when (e) {
            is SocketTimeoutException -> context.getString(R.string.error_server_timeout)
            is ConnectException -> context.getString(R.string.error_connection_failed)
            else -> context.getString(R.string.error_network_default, e.localizedMessage ?: "Unknown error")
        }

        _loadingState.update { LoadingState(isFinished = true, message = errorMessage) }
    }
    fun onServerAddressChanged(address: String) { _serverAddress.update { address } }
    fun onLoginChanged(newLogin: String) { _login.update { newLogin } }
    fun onPasswordChanged(newPassword: String) { _password.update { newPassword } }
    fun dismissLoadingDialog() { _loadingState.update { LoadingState() } }
    fun onNavigationHandled() { _loginSuccessful.update { false } }

    fun onFolderPicked(path: String) {
        _selectedFolder.update { path }
        // Сохраняем эту папку как единственную для сканирования
        settings.updateExtraScanFolders(setOf(path))
        settings.updateExcludedScanFolders(emptySet())
    }

    // --- Сетевая логика ---

    fun onLogin() {
        _serverUrlError.update { null }
        if (!isServerAddressValid(_serverAddress.value)) {
            _serverUrlError.update { context.getString(R.string.error_invalid_address_format) }
            return
        }

        viewModelScope.launch {
            _loadingState.update { LoadingState(isLoading = true, message = context.getString(R.string.loading_login)) }
            try {
                val request = AuthRequest(username = _login.value, password = _password.value)
                val response = authApi.login(_serverAddress.value, request)

                if (response.status.isSuccess()) {
                    val authResponse: AuthResponse = response.body()
                    settings.serverAddress = _serverAddress.value
                    settings.login = _login.value
                    settings.password = _password.value
                    settings.accessToken = authResponse.accessToken

                    _loadingState.update { LoadingState(isFinished = true, message = context.getString(R.string.login_success)) }
                    _loginSuccessful.update { true }
                } else {
                    _loadingState.update { LoadingState(isFinished = true, message = context.getString(R.string.error_invalid_credentials)) }
                }
            } catch (e: Exception) {
                handleNetworkException(e)
            }
        }
    }

    fun onRegister() {
        _serverUrlError.update { null }
        if (!isServerAddressValid(_serverAddress.value)) {
            _serverUrlError.update { context.getString(R.string.error_invalid_address_format) }
            return
        }

        viewModelScope.launch {
            _loadingState.update { LoadingState(isLoading = true, message = context.getString(R.string.loading_register)) }
            try {
                val request = AuthRequest(username = _login.value, password = _password.value)
                val response = authApi.register(_serverAddress.value, request)

                if (response.status.isSuccess()) {
                    _loadingState.update { LoadingState(isFinished = true, message = context.getString(R.string.register_success)) }
                } else {
                    val errorBody = response.bodyAsText()
                    val errorMessage = if (errorBody.contains("already exists", ignoreCase = true)) {
                        context.getString(R.string.error_user_exists)
                    } else {
                        context.getString(R.string.error_register_failed)
                    }
                    _loadingState.update { LoadingState(isFinished = true, message = errorMessage) }
                }
            } catch (e: Exception) {
                handleNetworkException(e)
            }
        }
    }

    // --- Вспомогательные и другие функции ---

    private fun isServerAddressValid(address: String): Boolean {
        val urlRegex = Regex("""^http://(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}):(\d{1,5})$""")
        return urlRegex.matches(address)
    }

    fun onFinishSetupClick() {
        setupState.isComplete = true
    }

    fun onAudioPermissionRequest(isGranted: Boolean) {
        _isAudioPermissionGranted.update { isGranted }
    }
}
