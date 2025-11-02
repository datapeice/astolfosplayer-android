package com.datapeice.astolfoplayer.app.presentation.components

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun ProviderText(
    providerText: String,
    uri: String,
    color: Color = LocalContentColor.current.copy(
        alpha = .5f
    ),
    style: TextStyle = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.SemiBold
    ),
    textAlign: TextAlign = TextAlign.Start,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    Text(
        text = buildAnnotatedString {
            append(providerText)
            addLink(
                clickable = LinkAnnotation.Clickable(
                    tag = "lyrics-provider",
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            textDecoration = TextDecoration.Underline
                        )
                    ),
                    linkInteractionListener = object :
                        LinkInteractionListener {
                        override fun onClick(link: LinkAnnotation) {
                            uriHandler.openUri(uri)
                        }
                    }
                ),
                providerText.indexOfLast { it == ' ' } + 1,
                providerText.length
            )
        },
        color = color,
        style = style,
        textAlign = textAlign,
        modifier = modifier
    )
}