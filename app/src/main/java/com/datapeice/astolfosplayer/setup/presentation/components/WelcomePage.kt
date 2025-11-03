package com.datapeice.astolfosplayer.setup.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.datapeice.astolfosplayer.R
import com.datapeice.astolfosplayer.core.presentation.AppDetails

@Composable
fun WelcomePage(
    onGetStartedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(28.dp)
    ) {
        val context = LocalContext.current

        AppDetails(
            modifier = Modifier.align(Alignment.Center)
        )

        Button(
            onClick = onGetStartedClick,
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Text(text = context.resources.getString(R.string.get_started))
        }
    }
}