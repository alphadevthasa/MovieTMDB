package com.thasa.moviedb.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
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
        composable(GENRES) {
            GenreScreen(
                onGenreClick = { id, name -> navController.navigate("movies/$id/${Uri.encode(name)}") },
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
                onMovieClick = { navController.navigate("detail/$it") },
                onBack = navController::popBackStack,
            )
        }
        composable(
            route = DETAIL,
            arguments = listOf(navArgument("movieId") { type = NavType.IntType }),
        ) {
            MovieDetailScreen(onBack = navController::popBackStack)
        }
    }
}
