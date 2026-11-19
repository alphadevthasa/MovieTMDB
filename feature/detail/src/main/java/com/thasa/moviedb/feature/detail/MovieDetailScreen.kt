package com.thasa.moviedb.feature.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.thasa.moviedb.core.domain.Resource
import com.thasa.moviedb.core.domain.model.MovieDetail
import com.thasa.moviedb.core.domain.model.Review
import com.thasa.moviedb.core.domain.model.Video
import com.thasa.moviedb.core.ui.component.ErrorView
import com.thasa.moviedb.core.ui.component.LoadingView
import com.thasa.moviedb.core.ui.component.PosterImage
import com.thasa.moviedb.core.ui.toMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    onBack: () -> Unit,
    viewModel: MovieDetailViewModel = hiltViewModel(),
) {
    val detail by viewModel.detail.collectAsStateWithLifecycle()
    val trailer by viewModel.trailer.collectAsStateWithLifecycle()
    val reviews = viewModel.reviews.collectAsLazyPagingItems()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text((detail as? Resource.Success)?.data?.title.orEmpty(), maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val current = detail) {
                Resource.Loading -> LoadingView()
                is Resource.Error -> ErrorView(error = current.error, onRetry = viewModel::retry)
                is Resource.Success -> DetailContent(current.data, trailer, reviews, viewModel::retryTrailer)
            }
        }
    }
}

@Composable
private fun DetailContent(
    detail: MovieDetail,
    trailer: Resource<Video?>,
    reviews: LazyPagingItems<Review>,
    onRetryTrailer: () -> Unit,
) {
    LazyColumn {
        item(key = "header") { Header(detail) }
        item(key = "trailer") { TrailerSection(trailer, onRetryTrailer) }
        item(key = "reviews-title") {
            Text("Reviews", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
        }
        reviewsSection(reviews)
    }
}

@Composable
private fun Header(detail: MovieDetail) {
    Column {
        PosterImage(
            url = detail.backdropUrl ?: detail.posterUrl,
            title = detail.title,
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
        )
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(detail.title, style = MaterialTheme.typography.headlineSmall)
            detail.tagline?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            val meta = listOfNotNull(
                detail.releaseDate?.take(4),
                detail.runtimeMinutes?.let { "${it / 60}h ${it % 60}m" },
                "★ %.1f".format(detail.rating),
            )
            Text(meta.joinToString("  •  "), style = MaterialTheme.typography.bodyMedium)
            if (detail.genres.isNotEmpty()) {
                Text(detail.genres.joinToString(", ") { it.name }, style = MaterialTheme.typography.bodySmall)
            }
            Text(detail.overview ?: "No overview available.", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun TrailerSection(trailer: Resource<Video?>, onRetry: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Trailer", style = MaterialTheme.typography.titleLarge)
        when (trailer) {
            Resource.Loading -> Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
            }
            is Resource.Error -> {
                Text(trailer.error.toMessage())
                Button(onClick = onRetry) { Text("Retry") }
            }
            is Resource.Success ->
                trailer.data?.let { TrailerPlayer(it.key) } ?: Text("No trailer available")
        }
    }
}
