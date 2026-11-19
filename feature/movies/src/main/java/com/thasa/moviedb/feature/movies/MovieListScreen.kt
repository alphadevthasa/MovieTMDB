package com.thasa.moviedb.feature.movies

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.thasa.moviedb.core.domain.model.Movie
import com.thasa.moviedb.core.ui.component.EmptyView
import com.thasa.moviedb.core.ui.component.ErrorView
import com.thasa.moviedb.core.ui.component.LoadingView
import com.thasa.moviedb.core.ui.component.PosterImage
import com.thasa.moviedb.core.ui.component.pagingFooter
import com.thasa.moviedb.core.ui.toUiError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieListScreen(
    genreName: String,
    onMovieClick: (movieId: Int) -> Unit,
    onBack: () -> Unit,
    viewModel: MovieListViewModel = hiltViewModel(),
) {
    val movies = viewModel.movies.collectAsLazyPagingItems()
    val refresh = movies.loadState.refresh

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(genreName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                movies.itemCount == 0 && refresh is LoadState.Loading -> LoadingView()
                movies.itemCount == 0 && refresh is LoadState.Error ->
                    ErrorView(error = refresh.error.toUiError(), onRetry = movies::retry)
                movies.itemCount == 0 && refresh is LoadState.NotLoading ->
                    EmptyView("No movies found for this genre")
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(count = movies.itemCount, key = movies.itemKey { it.id }) { index ->
                        movies[index]?.let { MovieRow(it, onClick = { onMovieClick(it.id) }) }
                    }
                    pagingFooter(movies.loadState.append, onRetry = movies::retry)
                }
            }
        }
    }
}

@Composable
private fun MovieRow(movie: Movie, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PosterImage(
                url = movie.posterUrl,
                title = movie.title,
                modifier = Modifier.size(width = 80.dp, height = 120.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(movie.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    text = listOfNotNull(movie.releaseYear, "★ %.1f".format(movie.rating)).joinToString("  •  "),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
