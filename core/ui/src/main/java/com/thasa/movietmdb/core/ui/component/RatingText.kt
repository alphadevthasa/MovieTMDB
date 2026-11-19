package com.thasa.movietmdb.core.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.thasa.movietmdb.core.ui.theme.MovieTmdbColors
import java.util.Locale

@Composable
fun RatingText(
    rating: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodySmall,
) {
    Text(
        text = "★ " + String.format(Locale.US, "%.1f", rating),
        color = MovieTmdbColors.RatingGreen,
        fontWeight = FontWeight.Bold,
        style = style,
        modifier = modifier,
    )
}
