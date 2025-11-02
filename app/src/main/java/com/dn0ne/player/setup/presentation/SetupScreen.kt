package com.dn0ne.player.setup.presentation

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
import com.dn0ne.player.setup.presentation.components.AudioPermissionPage
import com.dn0ne.player.setup.presentation.components.MusicScanPage
import com.dn0ne.player.setup.presentation.components.ServerSetupPage
import com.dn0ne.player.setup.presentation.components.WelcomePage

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
        composable<SetupPage.ServerSetup> {
            ServerSetupPage(
                onLoginClick = { server, login, password ->
                    // Вызвать функцию из ViewModel для логина
                    viewModel.onLogin(server, login, password)
                    // И перейти на следующий экран
                    navController.navigate(SetupPage.AudioPermission)
                },
                onRegisterClick = { server, login, password ->
                    // Вызвать функцию из ViewModel для регистрации
                    viewModel.onRegister(server, login, password)
                    // Можно остаться на том же экране и показать сообщение
                    // или перейти дальше, если регистрация автоматическая
                    navController.navigate(SetupPage.AudioPermission)
                },
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