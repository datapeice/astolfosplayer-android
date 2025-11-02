package com.datapeice.astolfoplayer.app.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.datapeice.astolfoplayer.R

@Composable
fun NothingYet(modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visibleState = MutableTransitionState(false).apply {
            targetState = true
        },
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = 300
            )
        )
    ) {

        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val context = LocalContext.current

            Icon(
                painter = painterResource(R.drawable.ic_launcher_monochrome),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer {
                        scaleX = 2f
                        scaleY = 2f
                    }
            )

            Text(
                text = context.resources.getString(R.string.nothing_yet),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}