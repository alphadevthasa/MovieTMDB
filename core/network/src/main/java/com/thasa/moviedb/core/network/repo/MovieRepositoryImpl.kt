package com.thasa.moviedb.core.network.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.thasa.moviedb.core.domain.Resource
import com.thasa.moviedb.core.domain.model.Genre
import com.thasa.moviedb.core.domain.model.Movie
import com.thasa.moviedb.core.domain.model.MovieDetail
import com.thasa.moviedb.core.domain.model.Review
import com.thasa.moviedb.core.domain.model.Video
import com.thasa.moviedb.core.domain.repository.MovieRepository
import com.thasa.moviedb.core.network.api.TmdbApi
import com.thasa.moviedb.core.network.mapper.firstYouTubeTrailer
import com.thasa.moviedb.core.network.mapper.toDomain
import com.thasa.moviedb.core.network.paging.MoviePagingSource
import com.thasa.moviedb.core.network.paging.ReviewPagingSource
import com.thasa.moviedb.core.network.toAppError
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class MovieRepositoryImpl @Inject constructor(
    private val api: TmdbApi,
) : MovieRepository {

    override suspend fun getGenres(): Resource<List<Genre>> =
        safeCall { api.getGenres().genres.map { it.toDomain() } }

    override fun moviesByGenre(genreId: Int): Flow<PagingData<Movie>> =
        Pager(PAGING_CONFIG) { MoviePagingSource(api, genreId) }.flow

    override suspend fun getMovieDetail(id: Int): Resource<MovieDetail> =
        safeCall { api.movieDetail(id).toDomain() }

    override suspend fun getTrailer(id: Int): Resource<Video?> =
        safeCall { api.videos(id).results.firstYouTubeTrailer() }

    override fun reviews(movieId: Int): Flow<PagingData<Review>> =
        Pager(PAGING_CONFIG) { ReviewPagingSource(api, movieId) }.flow

    private suspend inline fun <T> safeCall(block: () -> T): Resource<T> = try {
        Resource.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Resource.Error(e.toAppError())
    }

    private companion object {
        val PAGING_CONFIG = PagingConfig(pageSize = 20, enablePlaceholders = false)
    }
}
