package com.thasa.moviedb.feature.genres

import app.cash.turbine.test
import com.thasa.moviedb.core.domain.AppError
import com.thasa.moviedb.core.domain.Resource
import com.thasa.moviedb.core.domain.model.Genre
import com.thasa.moviedb.core.domain.repository.MovieRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
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

    @Before fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        coEvery { repo.getGenreBackdrops(any()) } returns Resource.Success(emptyList())
    }
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

    private val action = Genre(28, "Action")
    private val adventure = Genre(12, "Adventure")

    @Test
    fun backdrops_afterGenresLoad_mapsEachGenre() = runTest {
        coEvery { repo.getGenres() } returns Resource.Success(listOf(action, adventure))
        coEvery { repo.getGenreBackdrops(28) } returns Resource.Success(listOf("a"))
        coEvery { repo.getGenreBackdrops(12) } returns Resource.Success(listOf("b"))
        val vm = GenreViewModel(repo)
        advanceUntilIdle()
        assertEquals(mapOf(28 to "a", 12 to "b"), vm.backdrops.value)
    }

    @Test
    fun backdrops_sharedTopMovie_secondGenreGetsNextCandidate() = runTest {
        coEvery { repo.getGenres() } returns Resource.Success(listOf(action, adventure))
        coEvery { repo.getGenreBackdrops(28) } returns Resource.Success(listOf("x", "a"))
        coEvery { repo.getGenreBackdrops(12) } returns Resource.Success(listOf("x", "b"))
        val vm = GenreViewModel(repo)
        advanceUntilIdle()
        assertEquals(mapOf(28 to "x", 12 to "b"), vm.backdrops.value)
    }

    @Test
    fun backdrops_allCandidatesUsed_reusesFirst() = runTest {
        coEvery { repo.getGenres() } returns Resource.Success(listOf(action, adventure))
        coEvery { repo.getGenreBackdrops(any()) } returns Resource.Success(listOf("x"))
        val vm = GenreViewModel(repo)
        advanceUntilIdle()
        assertEquals(mapOf(28 to "x", 12 to "x"), vm.backdrops.value)
    }

    @Test
    fun backdrops_oneGenreFails_othersAssignedAndStateSuccess() = runTest {
        coEvery { repo.getGenres() } returns Resource.Success(listOf(action, adventure))
        coEvery { repo.getGenreBackdrops(28) } returns Resource.Success(listOf("a"))
        coEvery { repo.getGenreBackdrops(12) } returns Resource.Error(AppError.Server(500))
        val vm = GenreViewModel(repo)
        advanceUntilIdle()
        assertEquals(mapOf(28 to "a"), vm.backdrops.value)
        assertEquals(Resource.Success(listOf(action, adventure)), vm.state.value)
    }

    @Test
    fun backdrops_genresError_noBackdropRequests() = runTest {
        coEvery { repo.getGenres() } returns Resource.Error(AppError.NoNetwork)
        val vm = GenreViewModel(repo)
        advanceUntilIdle()
        coVerify(exactly = 0) { repo.getGenreBackdrops(any()) }
        assertEquals(emptyMap<Int, String>(), vm.backdrops.value)
    }

    @Test
    fun retry_clearsAndRefetchesBackdrops() = runTest {
        coEvery { repo.getGenres() } returns Resource.Success(listOf(action))
        coEvery { repo.getGenreBackdrops(28) } returnsMany listOf(
            Resource.Success(listOf("old")),
            Resource.Success(listOf("new")),
        )
        val vm = GenreViewModel(repo)
        advanceUntilIdle()
        assertEquals(mapOf(28 to "old"), vm.backdrops.value)
        vm.retry()
        assertEquals(emptyMap<Int, String>(), vm.backdrops.value)
        advanceUntilIdle()
        assertEquals(mapOf(28 to "new"), vm.backdrops.value)
    }

    @Test
    fun backdrops_oneGenreHangs_othersShownAfterTimeout() = runTest {
        coEvery { repo.getGenres() } returns Resource.Success(listOf(action, adventure))
        coEvery { repo.getGenreBackdrops(28) } returns Resource.Success(listOf("a"))
        coEvery { repo.getGenreBackdrops(12) } coAnswers { awaitCancellation() }
        val vm = GenreViewModel(repo)
        advanceTimeBy(5_001)
        runCurrent()
        assertEquals(mapOf(28 to "a"), vm.backdrops.value)
    }
}
