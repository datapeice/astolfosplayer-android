package com.datapeice.astolfosplayer.setup.presentation

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datapeice.astolfosplayer.R
import com.datapeice.astolfosplayer.core.api.AuthApi
import com.datapeice.astolfosplayer.core.data.MusicScanner
import com.datapeice.astolfosplayer.core.data.Settings
import com.datapeice.astolfosplayer.setup.data.SetupState
import io.grpc.StatusException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoadingState(
    val isLoading: Boolean = false,
    val isFinished: Boolean = false,
    val message: String = ""
)

class SetupViewModel(
    private val setupState: SetupState,
    private val getFoldersWithAudio: () -> Set<String>,
    val settings: Settings,
    val musicScanner: MusicScanner,
    private val authApi: AuthApi,
    private val context: Context
) : ViewModel() {

    val startDestination: SetupPage by mutableStateOf(
        if (!setupState.isComplete) SetupPage.Welcome else SetupPage.AudioPermission
    )

    private val _serverAddress = MutableStateFlow(settings.serverAddress)
    val serverAddress = _serverAddress.asStateFlow()

    private val _login = MutableStateFlow(settings.login)
    val login = _login.asStateFlow()

    private val _password = MutableStateFlow(settings.password)
    val password = _password.asStateFlow()

    private val _securityKey = MutableStateFlow("")
    val securityKey = _securityKey.asStateFlow()

    private val _serverUrlError = MutableStateFlow<String?>(null)
    val serverUrlError = _serverUrlError.asStateFlow()

    private val _loadingState = MutableStateFlow(LoadingState())
    val loadingState = _loadingState.asStateFlow()

    private val _loginSuccessful = MutableStateFlow(false)
    val loginSuccessful = _loginSuccessful.asStateFlow()

    private val _isAudioPermissionGranted = MutableStateFlow(false)
    val isAudioPermissionGranted = _isAudioPermissionGranted.asStateFlow()

    private val _selectedFolder = MutableStateFlow<String?>(null)
    val selectedFolder = _selectedFolder.asStateFlow()

    val isSetupComplete: Boolean
        get() = setupState.isComplete

    fun onServerAddressChanged(address: String) { _serverAddress.update { address } }
    fun onLoginChanged(newLogin: String) { _login.update { newLogin } }
    fun onPasswordChanged(newPassword: String) { _password.update { newPassword } }
    fun onSecurityKeyChanged(key: String) { _securityKey.update { key } }
    fun dismissLoadingDialog() { _loadingState.update { LoadingState() } }
    fun onNavigationHandled() { _loginSuccessful.update { false } }

    fun onFolderPicked(path: String) {
        _selectedFolder.update { path }
        settings.updateExtraScanFolders(setOf(path))
        settings.updateExcludedScanFolders(emptySet())
    }

    fun onLogin() {
        _serverUrlError.update { null }
        if (!isServerAddressValid(_serverAddress.value)) {
            _serverUrlError.update { context.getString(R.string.error_invalid_address_format) }
            return
        }

        viewModelScope.launch {
            _loadingState.update { LoadingState(isLoading = true, message = context.getString(R.string.loading_login)) }

            try {
                // ВАЖНО: Сохраняем адрес ДО попытки входа.
                // GrpcChannelProvider читает адрес из settings при создании канала.
                settings.serverAddress = _serverAddress.value

                // Теперь authApi создаст канал, используя уже сохраненный адрес
                val response = authApi.login(_login.value, _password.value)

                // Сохраняем остальные данные после успеха
                settings.login = _login.value
                settings.password = _password.value
                settings.accessToken = response.token

                _loadingState.update { LoadingState(isFinished = true, message = context.getString(R.string.login_success)) }
                _loginSuccessful.update { true }

            } catch (e: io.grpc.StatusException) {
                val errorMessage = when (e.status.code) {
                    io.grpc.Status.Code.UNAUTHENTICATED -> context.getString(R.string.error_invalid_credentials)
                    io.grpc.Status.Code.UNAVAILABLE -> context.getString(R.string.error_connection_failed) + " (Check IP/Port)"
                    io.grpc.Status.Code.DEADLINE_EXCEEDED -> context.getString(R.string.error_server_timeout)
                    else -> e.status.description ?: "gRPC Error: ${e.status.code}"
                }
                _loadingState.update { LoadingState(isFinished = true, message = errorMessage) }
            } catch (e: Exception) {
                _loadingState.update { LoadingState(isFinished = true, message = e.message ?: "Unknown error") }
            }
        }
    }

    fun onRegister() {
        _serverUrlError.update { null }
        if (!isServerAddressValid(_serverAddress.value)) {
            _serverUrlError.update { context.getString(R.string.error_invalid_address_format) }
            return
        }

        if (_securityKey.value.isBlank()) {
            _loadingState.update { LoadingState(isFinished = true, message = "Security key is required") }
            return
        }

        viewModelScope.launch {
            _loadingState.update { LoadingState(isLoading = true, message = context.getString(R.string.loading_register)) }

            try {
                // ВАЖНО: Сохраняем адрес ДО попытки регистрации
                settings.serverAddress = _serverAddress.value

                authApi.register(_login.value, _password.value, _securityKey.value)

                _loadingState.update { LoadingState(isFinished = true, message = context.getString(R.string.register_success)) }
            } catch (e: io.grpc.StatusException) {
                val errorMessage = when (e.status.code) {
                    io.grpc.Status.Code.ALREADY_EXISTS -> context.getString(R.string.error_user_exists)
                    io.grpc.Status.Code.PERMISSION_DENIED -> "Invalid security key"
                    io.grpc.Status.Code.UNAVAILABLE -> context.getString(R.string.error_connection_failed)
                    io.grpc.Status.Code.DEADLINE_EXCEEDED -> context.getString(R.string.error_server_timeout)
                    else -> e.status.description ?: "Registration failed"
                }
                _loadingState.update { LoadingState(isFinished = true, message = errorMessage) }
            } catch (e: Exception) {
                _loadingState.update { LoadingState(isFinished = true, message = e.message ?: "Unknown error") }
            }
        }
    }
    private fun isServerAddressValid(address: String): Boolean {
        // Проверка IPv4 (например: 78.10.162.140)
        val ipv4Regex = Regex("""^\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}$""")

        // Проверка домена (например: ap.datapeice.me, example.com, sub.domain.org)
        val domainRegex = Regex("""^([a-zA-Z0-9]([a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])?\.)+[a-zA-Z]{2,}$""")

        return ipv4Regex.matches(address) || domainRegex.matches(address)
    }


    fun onFinishSetupClick() {
        setupState.isComplete = true
    }

    fun onAudioPermissionRequest(isGranted: Boolean) {
        _isAudioPermissionGranted.update { isGranted }
    }
}
