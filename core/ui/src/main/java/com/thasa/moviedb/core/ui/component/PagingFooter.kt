package com.thasa.moviedb.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import com.thasa.moviedb.core.ui.theme.MovieDbColors
import com.thasa.moviedb.core.ui.toMessage
import com.thasa.moviedb.core.ui.toUiError

private const val LOADING_KEY = "paging-footer-loading"
private const val ERROR_KEY = "paging-footer-error"

/** Footer row for the end of a paged list: spinner while appending, inline retry on failure. */
fun LazyListScope.pagingFooter(appendState: LoadState, onRetry: () -> Unit) {
    when (appendState) {
        is LoadState.Loading -> item(key = LOADING_KEY) { FooterLoading() }
        is LoadState.Error -> item(key = ERROR_KEY) { FooterError(appendState, onRetry) }
        is LoadState.NotLoading -> Unit
    }
}

/** Grid version of [pagingFooter]; the footer spans the whole row. */
fun LazyGridScope.pagingFooter(appendState: LoadState, onRetry: () -> Unit) {
    when (appendState) {
        is LoadState.Loading -> item(key = LOADING_KEY, span = { GridItemSpan(maxLineSpan) }) { FooterLoading() }
        is LoadState.Error -> item(key = ERROR_KEY, span = { GridItemSpan(maxLineSpan) }) {
            FooterError(appendState, onRetry)
        }
        is LoadState.NotLoading -> Unit
    }
}

@Composable
private fun FooterLoading() {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FooterError(state: LoadState.Error, onRetry: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(state.error.toUiError().toMessage(), color = MovieDbColors.TextSecondary, modifier = Modifier.weight(1f))
        TextButton(onClick = onRetry) { Text("Retry") }
    }
}
