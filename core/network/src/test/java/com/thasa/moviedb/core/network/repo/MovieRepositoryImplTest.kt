package com.thasa.moviedb.core.network.repo

import androidx.paging.testing.asSnapshot
import com.thasa.moviedb.core.domain.AppError
import com.thasa.moviedb.core.domain.Resource
import com.thasa.moviedb.core.network.enqueueJson
import com.thasa.moviedb.core.network.moviePageJson
import com.thasa.moviedb.core.network.tmdbApi
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MovieRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repo: MovieRepositoryImpl

    @Before fun setUp() {
        server = MockWebServer().also { it.start() }
        repo = MovieRepositoryImpl(server.tmdbApi())
    }

    @After fun tearDown() { server.shutdown() }

    @Test
    fun getGenres_success_returnsMappedList() = runTest {
        server.enqueueJson("""{"genres":[{"id":28,"name":"Action"},{"id":12,"name":"Adventure"}]}""")
        val result = repo.getGenres() as Resource.Success
        assertEquals(listOf("Action", "Adventure"), result.data.map { it.name })
    }

    @Test
    fun getGenres_http401_returnsErrorUnauthorized() = runTest {
        server.enqueueJson("""{"status_code":7}""", 401)
        assertEquals(Resource.Error(AppError.Unauthorized), repo.getGenres())
    }

    @Test
    fun getGenres_ioFailure_returnsErrorNoNetwork() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
        assertEquals(Resource.Error(AppError.NoNetwork), repo.getGenres())
    }

    @Test
    fun getMovieDetail_success() = runTest {
        server.enqueueJson(
            """{"id":7,"title":"Seven","tagline":"","overview":"o","poster_path":"/p.jpg","backdrop_path":null,
            "vote_average":8.1,"release_date":"1995-09-22","runtime":127,"genres":[{"id":80,"name":"Crime"}]}""",
        )
        val detail = (repo.getMovieDetail(7) as Resource.Success).data
        assertEquals("Seven", detail.title)
        assertEquals(127, detail.runtimeMinutes)
        assertEquals(null, detail.tagline)
    }

    @Test
    fun getMovieDetail_http404_returnsErrorNotFound() = runTest {
        server.enqueueJson("""{"success":false}""", 404)
        assertEquals(Resource.Error(AppError.NotFound), repo.getMovieDetail(1))
    }

    @Test
    fun getTrailer_returnsFirstYouTubeTrailer() = runTest {
        server.enqueueJson(
            """{"results":[{"key":"a","name":"Clip","site":"YouTube","type":"Clip"},
            {"key":"b","name":"Official Trailer","site":"YouTube","type":"Trailer"}]}""",
        )
        val video = (repo.getTrailer(1) as Resource.Success).data
        assertEquals("b", video?.key)
    }

    @Test
    fun getTrailer_noYouTube_returnsSuccessNull() = runTest {
        server.enqueueJson("""{"results":[{"key":"a","name":"T","site":"Vimeo","type":"Trailer"}]}""")
        assertEquals(Resource.Success(null), repo.getTrailer(1))
    }

    @Test
    fun getTrailer_http500_returnsError() = runTest {
        server.enqueueJson("{}", 500)
        assertTrue(repo.getTrailer(1) is Resource.Error)
    }

    @Test
    fun moviesByGenre_asSnapshot_returnsFirstPageItems() = runTest {
        server.enqueueJson(moviePageJson(1, 1, 1, 2, 3))
        val items = repo.moviesByGenre(28).asSnapshot()
        assertEquals(listOf(1, 2, 3), items.map { it.id })
    }
}
