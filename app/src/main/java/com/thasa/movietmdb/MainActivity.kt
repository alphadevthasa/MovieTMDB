package com.thasa.movietmdb

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.thasa.movietmdb.core.ui.theme.MovieTmdbColors
import com.thasa.movietmdb.core.ui.theme.MovieTmdbTheme
import com.thasa.movietmdb.navigation.MovieTmdbNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            MovieTmdbTheme {
                // Solid backdrop behind screen transitions, so the cross-fade never shows the window colour.
                Surface(color = MovieTmdbColors.Background) {
                    MovieTmdbNavGraph()
                }
            }
        }
    }
}
