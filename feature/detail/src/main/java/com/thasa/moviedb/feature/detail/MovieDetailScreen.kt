package com.thasa.moviedb.feature.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import com.thasa.moviedb.core.ui.component.RatingText
import com.thasa.moviedb.core.ui.theme.MovieDbColors
import com.thasa.moviedb.core.ui.toMessage

private val TopBarHeight = 64.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    onBack: () -> Unit,
    viewModel: MovieDetailViewModel = hiltViewModel(),
) {
    val detail by viewModel.detail.collectAsStateWithLifecycle()
    val trailer by viewModel.trailer.collectAsStateWithLifecycle()
    val reviews = viewModel.reviews.collectAsLazyPagingItems()

    val listState = rememberLazyListState()
    // Solid top bar as soon as the title (measured, any length or density) would slide under it.
    val density = LocalDensity.current
    val barBottomPx = WindowInsets.statusBars.getTop(density) + with(density) { TopBarHeight.toPx() }
    var titleTopPx by remember { mutableFloatStateOf(Float.MAX_VALUE) }
    val scrolledPastBackdrop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset >= (titleTopPx - barBottomPx).coerceAtLeast(0f)
        }
    }
    val topBarColor by animateColorAsState(
        targetValue = if (scrolledPastBackdrop) MovieDbColors.Background else Color.Transparent,
        label = "topBarColor",
    )

    Surface(color = MovieDbColors.Background, contentColor = MovieDbColors.TextPrimary) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (val current = detail) {
                Resource.Loading -> LoadingView()
                is Resource.Error -> ErrorView(
                    error = current.error,
                    onRetry = viewModel::retry,
                    modifier = Modifier.statusBarsPadding().padding(top = TopBarHeight),
                )
                is Resource.Success -> DetailContent(
                    current.data, trailer, reviews, listState, viewModel::retryTrailer,
                    onTitlePositioned = { titleTopPx = it },
                )
            }
            // Drawn over the backdrop; the content scrolls underneath it.
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = topBarColor),
            )
        }
    }
}

@Composable
private fun DetailContent(
    detail: MovieDetail,
    trailer: Resource<Video?>,
    reviews: LazyPagingItems<Review>,
    listState: LazyListState,
    onRetryTrailer: () -> Unit,
    onTitlePositioned: (topPx: Float) -> Unit,
) {
    LazyColumn(state = listState, contentPadding = WindowInsets.navigationBars.asPaddingValues()) {
        item(key = "header") { Header(detail, onTitlePositioned) }
        item(key = "trailer") { TrailerSection(trailer, onRetryTrailer) }
        item(key = "reviews-title") { SectionTitle("Reviews", Modifier.padding(16.dp)) }
        reviewsSection(reviews)
    }
}

@Composable
private fun Header(detail: MovieDetail, onTitlePositioned: (topPx: Float) -> Unit) {
    Column {
        Box {
            PosterImage(
                url = detail.backdropUrl ?: detail.posterUrl,
                title = detail.title,
                shape = RectangleShape,
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.6f),
                            0.25f to Color.Transparent,
                            0.6f to Color.Transparent,
                            1f to MovieDbColors.Background,
                        ),
                    ),
            )
            Text(
                text = detail.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 16.dp)
                    .onGloballyPositioned { onTitlePositioned(it.positionInParent().y) },
            )
        }
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                RatingText(detail.rating, style = MaterialTheme.typography.bodyMedium)
                val meta = listOfNotNull(
                    detail.releaseDate?.take(4),
                    detail.runtimeMinutes?.let { "${it / 60}h ${it % 60}m" },
                )
                if (meta.isNotEmpty()) {
                    Text(meta.joinToString("  •  "), style = MaterialTheme.typography.bodyMedium, color = MovieDbColors.TextSecondary)
                }
            }
            if (detail.genres.isNotEmpty()) {
                Text(
                    text = detail.genres.joinToString(" • ") { it.name },
                    style = MaterialTheme.typography.bodySmall,
                    color = MovieDbColors.TextSecondary,
                )
            }
            detail.tagline?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic, color = MovieDbColors.TextSecondary)
            }
            Text(
                text = detail.overview ?: "No overview available.",
                style = MaterialTheme.typography.bodyMedium,
                color = MovieDbColors.TextLight,
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = modifier)
}

@Composable
private fun TrailerSection(trailer: Resource<Video?>, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SectionTitle("Trailer")
        when (trailer) {
            Resource.Loading -> Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
            }
            is Resource.Error -> {
                Text(trailer.error.toMessage(), color = MovieDbColors.TextSecondary)
                Button(onClick = onRetry) { Text("Retry") }
            }
            is Resource.Success ->
                trailer.data?.let { TrailerPlayer(it.key) }
                    ?: Text("No trailer available", color = MovieDbColors.TextSecondary)
        }
    }
}
