package com.thasa.moviedb.core.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.thasa.moviedb.core.ui.theme.MovieDbColors
import java.util.Locale

@Composable
fun RatingText(
    rating: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodySmall,
) {
    Text(
        text = "★ " + String.format(Locale.US, "%.1f", rating),
        color = MovieDbColors.RatingGreen,
        fontWeight = FontWeight.Bold,
        style = style,
        modifier = modifier,
    )
}
