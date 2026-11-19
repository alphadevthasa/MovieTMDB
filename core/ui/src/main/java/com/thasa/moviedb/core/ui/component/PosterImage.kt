package com.thasa.moviedb.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.thasa.moviedb.core.ui.theme.MovieDbColors

/** Image with a dark placeholder showing the title's first letter while loading, or when missing/failed. */
@Composable
fun PosterImage(
    url: String?,
    title: String?,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraSmall,
) {
    var showPlaceholder by remember(url) { mutableStateOf(true) }
    Box(
        modifier = modifier.clip(shape).background(MovieDbColors.SurfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        val letter = title?.trim()?.firstOrNull()?.uppercase()
        if (showPlaceholder && letter != null) {
            Text(
                text = letter,
                color = MovieDbColors.TextSecondary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium,
            )
        }
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                onSuccess = { showPlaceholder = false },
                onError = { showPlaceholder = true },
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}
