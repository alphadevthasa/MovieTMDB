package com.thasa.moviedb.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NetflixDarkScheme = darkColorScheme(
    primary = MovieDbColors.Red,
    onPrimary = MovieDbColors.TextPrimary,
    secondary = MovieDbColors.Red,
    onSecondary = MovieDbColors.TextPrimary,
    background = MovieDbColors.Background,
    onBackground = MovieDbColors.TextPrimary,
    surface = MovieDbColors.Background,
    onSurface = MovieDbColors.TextPrimary,
    surfaceVariant = MovieDbColors.SurfaceVariant,
    onSurfaceVariant = MovieDbColors.TextSecondary,
    surfaceContainer = MovieDbColors.Surface,
    surfaceContainerHigh = MovieDbColors.Surface,
    surfaceContainerHighest = MovieDbColors.Surface,
)

/** Always dark, Netflix-like theme; deliberately ignores the system light/dark setting. */
@Composable
fun MovieDbTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NetflixDarkScheme, content = content)
}
