package com.thasa.moviedb.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun PosterImage(url: String?, contentDescription: String?, modifier: Modifier = Modifier) {
    val placeholder = MaterialTheme.colorScheme.surfaceVariant
    if (url == null) {
        Box(modifier = modifier.clip(MaterialTheme.shapes.small).background(placeholder))
    } else {
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            placeholder = ColorPainter(placeholder),
            error = ColorPainter(placeholder),
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(MaterialTheme.shapes.small),
        )
    }
}
