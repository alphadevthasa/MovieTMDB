package com.thasa.moviedb.core.ui

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.LoadState
import com.thasa.moviedb.core.domain.AppError
import com.thasa.moviedb.core.ui.component.PosterImage
import com.thasa.moviedb.core.ui.component.RatingText
import com.thasa.moviedb.core.ui.component.pagingFooter
import com.thasa.moviedb.core.ui.theme.MovieDbTheme
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.Locale

class NetflixComponentsTest {
    @get:Rule val rule = createComposeRule()
    private val originalLocale: Locale = Locale.getDefault()

    @After fun restoreLocale() { Locale.setDefault(originalLocale) }

    @Test
    fun theme_isAlwaysDarkWithNetflixColors() {
        var background = Color.Unspecified
        var primary = Color.Unspecified
        rule.setContent {
            MovieDbTheme {
                background = MaterialTheme.colorScheme.background
                primary = MaterialTheme.colorScheme.primary
            }
        }
        rule.waitForIdle()
        assertEquals(Color(0xFF141414), background)
        assertEquals(Color(0xFFE50914), primary)
    }

    @Test
    fun posterImage_nullUrl_showsFirstLetterOfTitle() {
        rule.setContent { MovieDbTheme { PosterImage(url = null, title = "top gun") } }
        rule.onNodeWithText("T").assertIsDisplayed()
    }

    @Test
    fun posterImage_blankTitle_showsNoLetter() {
        rule.setContent { MovieDbTheme { PosterImage(url = null, title = "  ") } }
        rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text)).assertCountEquals(0)
    }

    @Test
    fun ratingText_usesDotDecimalInIndonesianLocale() {
        Locale.setDefault(Locale("in", "ID"))
        rule.setContent { MovieDbTheme { RatingText(7.5) } }
        rule.onNodeWithText("★ 7.5").assertExists()
    }

    @Test
    fun gridPagingFooter_error_showsRetryAndInvokesCallback() {
        var clicked = false
        rule.setContent {
            MovieDbTheme {
                LazyVerticalGrid(GridCells.Fixed(3)) {
                    pagingFooter(LoadState.Error(AppError.NoNetwork), onRetry = { clicked = true })
                }
            }
        }
        rule.onNodeWithText("Retry").performClick()
        assertTrue(clicked)
    }
}
