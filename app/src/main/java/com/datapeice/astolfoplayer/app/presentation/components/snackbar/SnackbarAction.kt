package com.datapeice.astolfoplayer.app.presentation.components.snackbar

import androidx.annotation.StringRes

data class SnackbarAction(
    @StringRes val name: Int,
    val action: () -> Unit
)