package com.thasa.moviedb.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.thasa.moviedb.feature.detail.MovieDetailScreen
import com.thasa.moviedb.feature.genres.GenreScreen
import com.thasa.moviedb.feature.movies.MovieListScreen

private const val GENRES = "genres"
private const val MOVIES = "movies/{genreId}/{genreName}"
private const val DETAIL = "detail/{movieId}"

@Composable
fun MovieDbNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = GENRES) {
        composable(GENRES) { entry ->
            GenreScreen(
                onGenreClick = { id, name ->
                    entry.runIfResumed { navController.navigate("movies/$id/${Uri.encode(name)}") }
                },
            )
        }
        composable(
            route = MOVIES,
            arguments = listOf(
                navArgument("genreId") { type = NavType.IntType },
                navArgument("genreName") { type = NavType.StringType },
            ),
        ) { entry ->
            MovieListScreen(
                genreName = entry.arguments?.getString("genreName").orEmpty(),
                onMovieClick = { entry.runIfResumed { navController.navigate("detail/$it") } },
                onBack = { entry.runIfResumed { navController.popBackStack() } },
            )
        }
        composable(
            route = DETAIL,
            arguments = listOf(navArgument("movieId") { type = NavType.IntType }),
        ) { entry ->
            MovieDetailScreen(onBack = { entry.runIfResumed { navController.popBackStack() } })
        }
    }
}

/**
 * Ignores taps that arrive while this destination is animating out (or in), so a double tap
 * can't pop twice (blank screen) or open the same screen twice.
 */
private fun NavBackStackEntry.runIfResumed(action: () -> Unit) {
    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) action()
}
