package com.thasa.moviedb.core.data.paging

import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import com.thasa.moviedb.core.domain.AppError
import com.thasa.moviedb.core.data.enqueueJson
import com.thasa.moviedb.core.data.moviePageJson
import com.thasa.moviedb.core.data.reviewPageJson
import com.thasa.moviedb.core.data.tmdbApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PagingSourceTest {
    private lateinit var server: MockWebServer

    @Before fun setUp() { server = MockWebServer().also { it.start() } }
    @After fun tearDown() { server.shutdown() }

    private fun <K : Any> refresh(key: K? = null) = LoadParams.Refresh(key, 20, false)

    @Test
    fun moviePagingSource_firstPage_nextKeyIs2() = runTest {
        server.enqueueJson(moviePageJson(1, 3, 1, 2))
        val result = MoviePagingSource(server.tmdbApi(), 28).load(refresh<Int>()) as LoadResult.Page
        assertEquals(listOf(1, 2), result.data.map { it.id })
        assertEquals(2, result.nextKey)
        assertNull(result.prevKey)
        assertEquals("/discover/movie?with_genres=28&page=1", server.takeRequest().path)
    }

    @Test
    fun moviePagingSource_lastPage_nextKeyNull() = runTest {
        server.enqueueJson(moviePageJson(3, 3, 9))
        val result = MoviePagingSource(server.tmdbApi(), 28).load(refresh(3)) as LoadResult.Page
        assertNull(result.nextKey)
    }

    @Test
    fun moviePagingSource_emptyResults_returnsEmptyPageNoNextKey() = runTest {
        server.enqueueJson(moviePageJson(1, 0))
        val result = MoviePagingSource(server.tmdbApi(), 28).load(refresh<Int>()) as LoadResult.Page
        assertTrue(result.data.isEmpty())
        assertNull(result.nextKey)
    }

    @Test
    fun moviePagingSource_http500_returnsLoadResultErrorWithServer500() = runTest {
        server.enqueueJson("{}", 500)
        val result = MoviePagingSource(server.tmdbApi(), 28).load(refresh<Int>())
        assertEquals(AppError.Server(500), (result as LoadResult.Error).throwable)
    }

    @Test
    fun moviePagingSource_page2Fails_page1StillLoaded() = runTest {
        server.enqueueJson(moviePageJson(1, 2, 1))
        server.enqueueJson("{}", 500)
        val source = MoviePagingSource(server.tmdbApi(), 28)
        val first = source.load(refresh<Int>()) as LoadResult.Page
        val second = source.load(LoadParams.Append(first.nextKey!!, 20, false))
        assertEquals(listOf(1), first.data.map { it.id })
        assertTrue(second is LoadResult.Error)
    }

    @Test
    fun moviePagingSource_movieRepeatedOnNextPage_isDroppedFromThatPage() = runTest {
        server.enqueueJson(moviePageJson(1, 2, 1, 2))
        server.enqueueJson(moviePageJson(2, 2, 2, 3))
        val source = MoviePagingSource(server.tmdbApi(), 28)
        source.load(refresh<Int>())
        val second = source.load(LoadParams.Append(2, 20, false)) as LoadResult.Page
        assertEquals(listOf(3), second.data.map { it.id })
    }

    @Test
    fun reviewPagingSource_firstPage_nextKeyIs2() = runTest {
        server.enqueueJson(reviewPageJson(1, 2, "r1"))
        val result = ReviewPagingSource(server.tmdbApi(), 5).load(refresh<Int>()) as LoadResult.Page
        assertEquals(listOf("r1"), result.data.map { it.id })
        assertEquals(2, result.nextKey)
        assertEquals("/movie/5/reviews?page=1", server.takeRequest().path)
    }

    @Test
    fun reviewPagingSource_lastPage_nextKeyNull() = runTest {
        server.enqueueJson(reviewPageJson(2, 2, "r9"))
        val result = ReviewPagingSource(server.tmdbApi(), 5).load(refresh(2)) as LoadResult.Page
        assertNull(result.nextKey)
    }

    @Test
    fun reviewPagingSource_empty_returnsEmptyPage() = runTest {
        server.enqueueJson(reviewPageJson(1, 0))
        val result = ReviewPagingSource(server.tmdbApi(), 5).load(refresh<Int>()) as LoadResult.Page
        assertTrue(result.data.isEmpty())
        assertNull(result.nextKey)
    }
}
