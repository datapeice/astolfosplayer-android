package com.datapeice.astolfosplayer.setup.presentation

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
import com.datapeice.astolfosplayer.setup.presentation.components.AudioPermissionPage
import com.datapeice.astolfosplayer.setup.presentation.components.LoadingDialog
import com.datapeice.astolfosplayer.setup.presentation.components.MusicScanPage
import com.datapeice.astolfosplayer.setup.presentation.components.ServerSetupPage
import com.datapeice.astolfosplayer.setup.presentation.components.WelcomePage

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
            val loadingState by viewModel.loadingState.collectAsState()
            val serverAddress by viewModel.serverAddress.collectAsState()
            val login by viewModel.login.collectAsState()
            val password by viewModel.password.collectAsState()
            val loginSuccessful by viewModel.loginSuccessful.collectAsState()

            ServerSetupPage(
                serverAddress = serverAddress,
                login = login,
                password = password,
                onServerAddressChange = viewModel::onServerAddressChanged,
                onLoginChange = viewModel::onLoginChanged,
                onPasswordChange = viewModel::onPasswordChanged,
                onLoginClick = {
                    viewModel.onLogin()
                },
                onRegisterClick = {
                    viewModel.onRegister()
                },
                errorMessage = serverError,
                modifier = modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background)
                    .safeDrawingPadding()
            )

            LoadingDialog(
                state = loadingState,
                onDismiss = {
                    // Если вход был успешным, переходим на следующий экран
                    if (loginSuccessful) {
                        viewModel.onNavigationHandled()
                        navController.navigate(SetupPage.AudioPermission)
                    }
                    viewModel.dismissLoadingDialog()
                }
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
            // Используем новое состояние для ОДНОЙ папки
            // Используем новое состояние для ОДНОЙ папки
            val selectedFolder by viewModel.selectedFolder.collectAsState()

            MusicScanPage(
                selectedFolderUriString = selectedFolder, // <-- ИСПРАВЛЕНО
                onPickFolderClick = { onFolderPick(true) },
                onFinishClick = {
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
