package com.thasa.movietmdb

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import com.thasa.movietmdb.core.domain.AppError
import com.thasa.movietmdb.core.ui.toMessage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class GenreFlowTest {
    @get:Rule(order = 0) val hilt = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun genreFlow_errorThenRetry_showsGenres() {
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Retry").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(AppError.NoNetwork.toMessage()).assertIsDisplayed()

        compose.onNodeWithText("Retry").performClick()

        compose.waitUntil(5_000) { compose.onAllNodesWithText("Action").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Action").assertIsDisplayed()
    }

    @Test
    fun genreScreen_showsWordmarkAndGenresHeading() {
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Retry").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Retry").performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Action").fetchSemanticsNodes().isNotEmpty() }

        compose.onNodeWithText("Movie TMDB").assertIsDisplayed()
        compose.onNodeWithText("Genres").assertIsDisplayed()
    }
}
