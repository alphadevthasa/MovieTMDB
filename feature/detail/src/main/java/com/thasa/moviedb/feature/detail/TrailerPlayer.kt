package com.thasa.moviedb.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.thasa.moviedb.core.ui.component.PosterImage
import com.thasa.moviedb.core.ui.theme.MovieDbColors
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

/**
 * Shows the trailer's YouTube thumbnail with a play button. The (heavy) WebView player is only created
 * after the user taps, so opening the detail screen never blocks the main thread on WebView start-up.
 */
@Composable
fun TrailerCard(videoKey: String, modifier: Modifier = Modifier) {
    var started by rememberSaveable(videoKey) { mutableStateOf(false) }
    if (started) {
        TrailerPlayer(videoKey = videoKey, autoPlay = true, modifier = modifier)
        return
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(MaterialTheme.shapes.extraSmall)
            .clickable { started = true }
            .semantics(mergeDescendants = true) {
                contentDescription = "Play trailer"
                role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        PosterImage(
            url = "https://img.youtube.com/vi/$videoKey/hqdefault.jpg",
            title = null,
            modifier = Modifier.matchParentSize(),
        )
        Box(modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.25f)))
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).background(MovieDbColors.Red),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
        }
    }
}

/** Plays a YouTube trailer in-app; falls back to opening YouTube if the embedded player fails. */
@Composable
fun TrailerPlayer(videoKey: String, autoPlay: Boolean, modifier: Modifier = Modifier) {
    var failed by remember(videoKey) { mutableStateOf(false) }
    val context = LocalContext.current

    if (failed) {
        Column(modifier = modifier.fillMaxWidth()) {
            Text("The trailer can't be played here.")
            Button(onClick = {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoKey")),
                )
            }) { Text("Open on YouTube") }
        }
        return
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val playerView = remember(videoKey) {
        YouTubePlayerView(context).apply {
            addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    if (autoPlay) youTubePlayer.loadVideo(videoKey, 0f) else youTubePlayer.cueVideo(videoKey, 0f)
                }

                override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                    failed = true
                }
            })
        }
    }
    DisposableEffect(playerView) {
        lifecycle.addObserver(playerView)
        onDispose {
            lifecycle.removeObserver(playerView)
            playerView.release()
        }
    }
    AndroidView(factory = { playerView }, modifier = modifier.fillMaxWidth().aspectRatio(16f / 9f))
}
