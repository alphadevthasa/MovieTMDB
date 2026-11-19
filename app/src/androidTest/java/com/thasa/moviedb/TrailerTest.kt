package com.thasa.moviedb

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.thasa.moviedb.core.domain.model.Video
import com.thasa.moviedb.core.domain.repository.MovieRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class TrailerTest {
    @get:Rule(order = 0) val hilt = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>()

    @Inject lateinit var repository: MovieRepository

    @Before fun setUp() {
        hilt.inject()
        (repository as FakeMovieRepository).trailer = Video("abc123", "Official Trailer")
    }

    @Test
    fun trailer_showsThumbnailFirst_andCreatesPlayerOnlyAfterTap() {
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Retry").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Retry").performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Action").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Action").performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithText(LONG_TITLE).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(LONG_TITLE).performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Overview text").fetchSemanticsNodes().isNotEmpty() }

        compose.onNode(hasScrollAction()).performScrollToNode(hasContentDescription("Play trailer"))
        compose.onNodeWithContentDescription("Play trailer").assertIsDisplayed()

        compose.onNodeWithContentDescription("Play trailer").performClick()
        compose.waitUntil(5_000) {
            compose.onAllNodesWithContentDescription("Play trailer").fetchSemanticsNodes().isEmpty()
        }
    }
}
