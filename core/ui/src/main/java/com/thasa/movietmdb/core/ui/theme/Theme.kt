package com.thasa.movietmdb.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NetflixDarkScheme = darkColorScheme(
    primary = MovieTmdbColors.Red,
    onPrimary = MovieTmdbColors.TextPrimary,
    secondary = MovieTmdbColors.Red,
    onSecondary = MovieTmdbColors.TextPrimary,
    background = MovieTmdbColors.Background,
    onBackground = MovieTmdbColors.TextPrimary,
    surface = MovieTmdbColors.Background,
    onSurface = MovieTmdbColors.TextPrimary,
    surfaceVariant = MovieTmdbColors.SurfaceVariant,
    onSurfaceVariant = MovieTmdbColors.TextSecondary,
    surfaceContainer = MovieTmdbColors.Surface,
    surfaceContainerHigh = MovieTmdbColors.Surface,
    surfaceContainerHighest = MovieTmdbColors.Surface,
)

/** Always dark, Netflix-like theme; deliberately ignores the system light/dark setting. */
@Composable
fun MovieTmdbTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NetflixDarkScheme, content = content)
}
