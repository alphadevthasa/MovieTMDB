package com.thasa.moviedb.feature.genres

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thasa.moviedb.core.domain.Resource
import com.thasa.moviedb.core.domain.model.Genre
import com.thasa.moviedb.core.ui.component.EmptyView
import com.thasa.moviedb.core.ui.component.ErrorView
import com.thasa.moviedb.core.ui.component.LoadingView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreScreen(
    onGenreClick: (genreId: Int, genreName: String) -> Unit,
    viewModel: GenreViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Genres") }) },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val current = state) {
                Resource.Loading -> LoadingView()
                is Resource.Error -> ErrorView(error = current.error, onRetry = viewModel::retry)
                is Resource.Success ->
                    if (current.data.isEmpty()) {
                        EmptyView("No genres available")
                    } else {
                        GenreGrid(genres = current.data, onGenreClick = onGenreClick)
                    }
            }
        }
    }
}

@Composable
private fun GenreGrid(genres: List<Genre>, onGenreClick: (Int, String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(genres, key = { it.id }) { genre ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clickable { onGenreClick(genre.id, genre.name) },
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(72.dp), contentAlignment = Alignment.Center) {
                    Text(text = genre.name, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
