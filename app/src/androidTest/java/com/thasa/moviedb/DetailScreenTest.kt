package com.thasa.moviedb

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasScrollAction
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class DetailScreenTest {
    @get:Rule(order = 0) val hilt = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun detail_showsGenresLineSectionsAndEmptyStates() {
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Retry").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Retry").performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Action").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Action").performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithText(LONG_TITLE).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(LONG_TITLE).performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Overview text").fetchSemanticsNodes().isNotEmpty() }

        compose.onNodeWithText("Action • Drama").assertExists()
        compose.onNode(hasScrollAction()).performScrollToNode(hasText("No reviews yet"))
        compose.onNodeWithText("Trailer").assertExists()
        compose.onNodeWithText("No trailer available").assertExists()
        compose.onNodeWithText("Reviews").assertExists()
        compose.onNodeWithContentDescription("Back").assertIsDisplayed()
    }
}
