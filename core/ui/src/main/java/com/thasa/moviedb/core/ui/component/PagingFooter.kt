package com.thasa.moviedb.core.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState

import com.thasa.moviedb.core.ui.toMessage
import com.thasa.moviedb.core.ui.toUiError

/** Footer row for the end of a paged list: spinner while appending, inline retry on failure. */
fun LazyListScope.pagingFooter(appendState: LoadState, onRetry: () -> Unit) {
    when (appendState) {
        is LoadState.Loading -> item(key = "paging-footer-loading") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.Center,
            ) { CircularProgressIndicator() }
        }
        is LoadState.Error -> item(key = "paging-footer-error") {
            val error = appendState.error.toUiError()
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(error.toMessage(), modifier = Modifier.weight(1f))
                TextButton(onClick = onRetry) { Text("Retry") }
            }
        }
        is LoadState.NotLoading -> Unit
    }
}
