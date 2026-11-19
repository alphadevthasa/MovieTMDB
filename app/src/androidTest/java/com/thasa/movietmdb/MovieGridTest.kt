package com.thasa.movietmdb

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class MovieGridTest {
    @get:Rule(order = 0) val hilt = HiltAndroidRule(this)
    @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun movieGrid_showsPosterLetterTitleAndRating() {
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Retry").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Retry").performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithText("Action").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Action").performClick()
        compose.waitUntil(5_000) { compose.onAllNodesWithText(LONG_TITLE).fetchSemanticsNodes().isNotEmpty() }

        compose.onNodeWithText("★ 8.2").assertIsDisplayed()
        compose.onNodeWithText("T").assertIsDisplayed()
        compose.onNodeWithText(LONG_TITLE).assertExists()
    }
}
