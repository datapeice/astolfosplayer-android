package com.datapeice.astolfoplayer.setup.presentation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.datapeice.astolfoplayer.setup.presentation.components.AudioPermissionPage
import com.datapeice.astolfoplayer.setup.presentation.components.MusicScanPage
import com.datapeice.astolfoplayer.setup.presentation.components.ServerSetupPage
import com.datapeice.astolfoplayer.setup.presentation.components.WelcomePage

@Composable
fun SetupScreen(
    viewModel: SetupViewModel,
    requestAudioPermission: () -> Unit,
    onFolderPick: (scan: Boolean) -> Unit,
    onFinishSetupClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val startDestination = viewModel.startDestination

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it })
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it })
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it })
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it })

        }
    ) {
        composable<SetupPage.Welcome> {
            WelcomePage(
                onGetStartedClick = {
                    navController.navigate(SetupPage.ServerSetup)
                },
                modifier = modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background)
                    .safeDrawingPadding()
            )
        }
// В файле SetupScreen.kt

        composable<SetupPage.ServerSetup> {
            val serverError by viewModel.serverUrlError.collectAsState()
            // --- ПОЛУЧАЕМ ЗНАЧЕНИЯ ДЛЯ ПОЛЕЙ ИЗ VIEWMODEL ---
            val serverAddress by viewModel.serverAddress.collectAsState()
            val login by viewModel.login.collectAsState()
            val password by viewModel.password.collectAsState()

            ServerSetupPage(
                // Передаем значения в UI
                serverAddress = serverAddress,
                login = login,
                password = password,

                // Передаем функции-обработчики из ViewModel
                onServerAddressChange = viewModel::onServerAddressChanged,
                onLoginChange = viewModel::onLoginChanged,
                onPasswordChange = viewModel::onPasswordChanged,

                // Обновляем вызов onLoginClick
                onLoginClick = {
                    if (viewModel.onLogin(serverAddress, login, password)) {
                        navController.navigate(SetupPage.AudioPermission)
                    }
                },
                onRegisterClick = {
                    if (viewModel.onRegister(serverAddress, login, password)) {
                        navController.navigate(SetupPage.AudioPermission)
                    }
                },
                errorMessage = serverError,
                modifier = modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background)
                    .safeDrawingPadding()
            )

        }

        composable<SetupPage.AudioPermission> {
            val isAudioPermissionGranted = viewModel.isAudioPermissionGranted.collectAsState()
            AudioPermissionPage(
                onGrantAudioPermissionClick = requestAudioPermission,
                onNextClick = {
                    if (viewModel.isSetupComplete) {
                        viewModel.onFinishSetupClick()
                        onFinishSetupClick()
                    } else {
                        navController.navigate(SetupPage.MusicScan)
                    }
                },
                isAudioPermissionGrantedState = isAudioPermissionGranted,
                modifier = modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background)
                    .safeDrawingPadding()
            )
        }

        composable<SetupPage.MusicScan> {
            val foldersWithAudio by viewModel.foldersWithAudio.collectAsState()
            MusicScanPage(
                settings = viewModel.settings,
                musicScanner = viewModel.musicScanner,
                onFolderPick = onFolderPick,
                foldersWithAudio = foldersWithAudio,
                onScanFoldersClick = viewModel::onScanFoldersClick,
                onNextClick = {
                    viewModel.onFinishSetupClick()
                    onFinishSetupClick()
                },
                modifier = modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background)
                    .safeDrawingPadding()
            )
        }
    }
}