package com.thasa.moviedb.feature.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.thasa.moviedb.core.domain.model.Review
import com.thasa.moviedb.core.ui.component.PosterImage
import com.thasa.moviedb.core.ui.component.pagingFooter
import com.thasa.moviedb.core.ui.toMessage
import com.thasa.moviedb.core.ui.toUiError

private const val COLLAPSED_LINES = 6
private const val LONG_REVIEW_CHARS = 280

/** Reviews as items of the parent lazy list, so the screen has a single scroll container. */
fun LazyListScope.reviewsSection(reviews: LazyPagingItems<Review>) {
    val refresh = reviews.loadState.refresh
    when {
        reviews.itemCount == 0 && refresh is LoadState.Loading -> item(key = "reviews-loading") {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
            }
        }
        reviews.itemCount == 0 && refresh is LoadState.Error -> item(key = "reviews-error") {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(refresh.error.toUiError().toMessage())
                Button(onClick = { reviews.retry() }) { Text("Retry") }
            }
        }
        reviews.itemCount == 0 -> item(key = "reviews-empty") {
            Text("No reviews yet", modifier = Modifier.padding(16.dp))
        }
        else -> {
            items(count = reviews.itemCount, key = reviews.itemKey { it.id }) { index ->
                reviews[index]?.let { ReviewCard(it) }
            }
            pagingFooter(reviews.loadState.append, onRetry = { reviews.retry() })
        }
    }
}

@Composable
private fun ReviewCard(review: Review) {
    var expanded by remember(review.id) { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PosterImage(review.avatarUrl, contentDescription = null, modifier = Modifier.size(40.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(review.author, style = MaterialTheme.typography.titleSmall)
                    val meta = listOfNotNull(review.createdAt?.take(10), review.rating?.let { "★ %.1f".format(it) })
                    if (meta.isNotEmpty()) {
                        Text(meta.joinToString("  •  "), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Text(
                text = review.content,
                maxLines = if (expanded) Int.MAX_VALUE else COLLAPSED_LINES,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (review.content.length > LONG_REVIEW_CHARS) {
                TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Show less" else "Read more") }
            }
        }
    }
}
