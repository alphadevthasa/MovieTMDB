package com.thasa.moviedb.feature.detail

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

/** Plays a YouTube trailer in-app; falls back to opening YouTube if the embedded player fails. */
@Composable
fun TrailerPlayer(videoKey: String, modifier: Modifier = Modifier) {
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
                    youTubePlayer.cueVideo(videoKey, 0f)
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
