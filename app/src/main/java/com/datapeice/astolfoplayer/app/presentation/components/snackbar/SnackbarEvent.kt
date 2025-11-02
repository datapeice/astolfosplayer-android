package com.datapeice.astolfoplayer.app.presentation.components.snackbar

import androidx.annotation.StringRes

data class SnackbarEvent(
    @StringRes val message: Int,
    val action: SnackbarAction? = null
)