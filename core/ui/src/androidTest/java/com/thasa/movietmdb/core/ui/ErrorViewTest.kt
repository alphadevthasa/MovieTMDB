package com.thasa.movietmdb.core.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.thasa.movietmdb.core.domain.AppError
import com.thasa.movietmdb.core.ui.component.ErrorView
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ErrorViewTest {
    @get:Rule val rule = createComposeRule()

    @Test
    fun errorView_showsMessageAndRetryInvokesCallback() {
        var clicked = false
        rule.setContent { ErrorView(error = AppError.NoNetwork, onRetry = { clicked = true }) }

        rule.onNodeWithText(AppError.NoNetwork.toMessage()).assertIsDisplayed()
        rule.onNodeWithText("Retry").performClick()

        assertTrue(clicked)
    }

    @Test
    fun errorMessages_areDistinctPerVariant() {
        val messages = listOf(
            AppError.NoNetwork, AppError.Timeout, AppError.Unauthorized,
            AppError.NotFound, AppError.Server(500), AppError.Unknown(null),
        ).map { it.toMessage() }
        assertTrue(messages.toSet().size == messages.size)
    }
}
