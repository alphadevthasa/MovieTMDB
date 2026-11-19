package com.thasa.moviedb.feature.genres

import app.cash.turbine.test
import com.thasa.moviedb.core.domain.AppError
import com.thasa.moviedb.core.domain.Resource
import com.thasa.moviedb.core.domain.model.Genre
import com.thasa.moviedb.core.domain.repository.MovieRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GenreViewModelTest {
    private val repo = mockk<MovieRepository>()
    private val genres = listOf(Genre(28, "Action"), Genre(12, "Adventure"))

    @Before fun setUp() { Dispatchers.setMain(StandardTestDispatcher()) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun init_success_emitsLoadingThenSuccess() = runTest {
        coEvery { repo.getGenres() } returns Resource.Success(genres)
        GenreViewModel(repo).state.test {
            assertEquals(Resource.Loading, awaitItem())
            assertEquals(Resource.Success(genres), awaitItem())
        }
    }

    @Test
    fun init_error_emitsLoadingThenError() = runTest {
        coEvery { repo.getGenres() } returns Resource.Error(AppError.NoNetwork)
        GenreViewModel(repo).state.test {
            assertEquals(Resource.Loading, awaitItem())
            assertEquals(Resource.Error(AppError.NoNetwork), awaitItem())
        }
    }

    @Test
    fun retry_afterError_emitsLoadingThenSuccess() = runTest {
        coEvery { repo.getGenres() } returnsMany listOf(Resource.Error(AppError.Timeout), Resource.Success(genres))
        val vm = GenreViewModel(repo)
        vm.state.test {
            assertEquals(Resource.Loading, awaitItem())
            assertEquals(Resource.Error(AppError.Timeout), awaitItem())
            vm.retry()
            assertEquals(Resource.Loading, awaitItem())
            assertEquals(Resource.Success(genres), awaitItem())
        }
    }

    @Test
    fun init_emptyList_emitsSuccessWithEmptyList() = runTest {
        coEvery { repo.getGenres() } returns Resource.Success(emptyList())
        GenreViewModel(repo).state.test {
            assertEquals(Resource.Loading, awaitItem())
            assertEquals(Resource.Success(emptyList<Genre>()), awaitItem())
        }
    }
}
