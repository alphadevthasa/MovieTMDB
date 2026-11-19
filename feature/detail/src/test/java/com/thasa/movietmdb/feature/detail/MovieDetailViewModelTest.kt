package com.thasa.movietmdb.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.thasa.movietmdb.core.domain.AppError
import com.thasa.movietmdb.core.domain.Resource
import com.thasa.movietmdb.core.domain.model.MovieDetail
import com.thasa.movietmdb.core.domain.model.Review
import com.thasa.movietmdb.core.domain.model.Video
import com.thasa.movietmdb.core.domain.repository.MovieRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailViewModelTest {
    private val repo = mockk<MovieRepository>()
    private val detail = MovieDetail(
        id = 7, title = "Seven", tagline = null, overview = null, posterUrl = null, backdropUrl = null,
        rating = 8.1, releaseDate = "1995-09-22", runtimeMinutes = 127, genres = emptyList(),
    )
    private val video = Video("abc123", "Official Trailer")

    @Before fun setUp() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private fun viewModel() = MovieDetailViewModel(SavedStateHandle(mapOf("movieId" to 7)), repo)

    private fun givenDefaults(
        detailResult: Resource<MovieDetail> = Resource.Success(detail),
        trailerResult: Resource<Video?> = Resource.Success(video),
    ) {
        coEvery { repo.getMovieDetail(7) } returns detailResult
        coEvery { repo.getTrailer(7) } returns trailerResult
        every { repo.reviews(7) } returns reviewPages()
    }

    private fun review(id: Int) = Review("r$id", "author$id", null, null, "content", null)

    private fun reviewPages(vararg pages: List<Review>) = Pager(
        PagingConfig(pageSize = 2, initialLoadSize = 2, prefetchDistance = 1, enablePlaceholders = false),
    ) {
        object : PagingSource<Int, Review>() {
            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Review> {
                val page = params.key ?: 0
                val data = pages.getOrElse(page) { emptyList() }
                return LoadResult.Page(data, null, if (page + 1 < pages.size) page + 1 else null)
            }
            override fun getRefreshKey(state: PagingState<Int, Review>): Int? = null
        }
    }.flow

    @Test
    fun detail_success_emitsDetail() = runTest {
        givenDefaults()
        viewModel().detail.test {
            assertEquals(Resource.Loading, awaitItem())
            assertEquals(Resource.Success(detail), awaitItem())
        }
    }

    @Test
    fun detail_http404_emitsErrorNotFound() = runTest {
        givenDefaults(detailResult = Resource.Error(AppError.NotFound))
        viewModel().detail.test {
            assertEquals(Resource.Loading, awaitItem())
            assertEquals(Resource.Error(AppError.NotFound), awaitItem())
        }
    }

    @Test
    fun detail_retry_afterError_emitsSuccess() = runTest {
        givenDefaults()
        coEvery { repo.getMovieDetail(7) } returnsMany listOf(Resource.Error(AppError.NoNetwork), Resource.Success(detail))
        val vm = viewModel()
        vm.detail.test {
            assertEquals(Resource.Loading, awaitItem())
            assertEquals(Resource.Error(AppError.NoNetwork), awaitItem())
            vm.retry()
            assertEquals(Resource.Loading, awaitItem())
            assertEquals(Resource.Success(detail), awaitItem())
        }
    }

    @Test
    fun trailer_found_emitsVideo() = runTest {
        givenDefaults()
        viewModel().trailer.test {
            assertEquals(Resource.Loading, awaitItem())
            assertEquals(Resource.Success(video), awaitItem())
        }
    }

    @Test
    fun trailer_none_emitsSuccessNull() = runTest {
        givenDefaults(trailerResult = Resource.Success(null))
        viewModel().trailer.test {
            assertEquals(Resource.Loading, awaitItem())
            assertEquals(Resource.Success(null), awaitItem())
        }
    }

    @Test
    fun trailer_error_doesNotAffectDetailState() = runTest {
        givenDefaults(trailerResult = Resource.Error(AppError.Server(500)))
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(Resource.Success(detail), vm.detail.value)
        assertEquals(Resource.Error(AppError.Server(500)), vm.trailer.value)
    }

    @Test
    fun trailer_retry_afterError_reloadsOnlyTrailer() = runTest {
        givenDefaults()
        coEvery { repo.getTrailer(7) } returnsMany listOf(Resource.Error(AppError.Timeout), Resource.Success(video))
        val vm = viewModel()
        advanceUntilIdle()
        assertEquals(Resource.Error(AppError.Timeout), vm.trailer.value)
        vm.retryTrailer()
        advanceUntilIdle()
        assertEquals(Resource.Success(video), vm.trailer.value)
        assertEquals(Resource.Success(detail), vm.detail.value)
        io.mockk.coVerify(exactly = 1) { repo.getMovieDetail(7) }
    }

    @Test
    fun reviews_scrollToEnd_loadsSecondPage() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        givenDefaults()
        every { repo.reviews(7) } returns reviewPages(listOf(review(1), review(2)), listOf(review(3), review(4)))
        val items = viewModel().reviews.asSnapshot { scrollTo(index = 3) }
        assertTrue(items.map { it.id }.containsAll(listOf("r3", "r4")))
    }

    @Test
    fun reviews_empty_emitsNoItems() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        givenDefaults()
        every { repo.reviews(7) } returns reviewPages(emptyList())
        assertTrue(viewModel().reviews.asSnapshot().isEmpty())
    }
}
