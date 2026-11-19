package com.thasa.moviedb.feature.movies

import androidx.lifecycle.SavedStateHandle
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.testing.asSnapshot
import com.thasa.moviedb.core.domain.model.Movie
import com.thasa.moviedb.core.domain.repository.MovieRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieListViewModelTest {
    private val repo = mockk<MovieRepository>()

    @Before fun setUp() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun movie(id: Int) = Movie(id, "Movie $id", null, 7.0, "2020")

    /** Two pages of two movies: [1, 2] then [3, 4]. */
    private fun twoPageFlow() = Pager(
        PagingConfig(pageSize = 2, initialLoadSize = 2, prefetchDistance = 1, enablePlaceholders = false),
    ) {
        object : PagingSource<Int, Movie>() {
            private val pages = listOf(listOf(movie(1), movie(2)), listOf(movie(3), movie(4)))
            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
                val page = params.key ?: 0
                return LoadResult.Page(pages[page], null, if (page + 1 < pages.size) page + 1 else null)
            }
            override fun getRefreshKey(state: PagingState<Int, Movie>): Int? = null
        }
    }.flow

    private fun viewModel(genreId: Int = 28) =
        MovieListViewModel(SavedStateHandle(mapOf("genreId" to genreId)), repo)

    @Test
    fun movies_firstPage_emitsItems() = runTest {
        every { repo.moviesByGenre(28) } returns twoPageFlow()
        val items = viewModel().movies.asSnapshot()
        assertEquals(listOf(1, 2), items.take(2).map { it.id })
    }

    @Test
    fun movies_scrollToEnd_loadsSecondPage() = runTest {
        every { repo.moviesByGenre(28) } returns twoPageFlow()
        val items = viewModel().movies.asSnapshot { scrollTo(index = 3) }
        assertTrue(items.map { it.id }.containsAll(listOf(3, 4)))
    }

    @Test
    fun movies_usesGenreIdFromSavedState() = runTest {
        every { repo.moviesByGenre(any()) } returns flowOf(PagingData.empty())
        viewModel(genreId = 35)
        verify { repo.moviesByGenre(35) }
    }
}
