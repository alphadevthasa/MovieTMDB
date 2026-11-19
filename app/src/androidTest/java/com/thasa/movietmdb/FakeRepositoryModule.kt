package com.thasa.movietmdb

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.thasa.movietmdb.core.domain.AppError
import com.thasa.movietmdb.core.domain.Resource
import com.thasa.movietmdb.core.domain.model.Genre
import com.thasa.movietmdb.core.domain.model.Movie
import com.thasa.movietmdb.core.domain.model.MovieDetail
import com.thasa.movietmdb.core.domain.model.Review
import com.thasa.movietmdb.core.domain.model.Video
import com.thasa.movietmdb.core.domain.repository.MovieRepository
import com.thasa.movietmdb.core.data.di.RepositoryModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Singleton

const val LONG_TITLE = "Top Gun: Maverick and a Very Long Subtitle"

/** Fails the first genre request with [AppError.NoNetwork], then succeeds. */
class FakeMovieRepository : MovieRepository {
    private var genreCalls = 0

    override suspend fun getGenres(): Resource<List<Genre>> =
        if (genreCalls++ == 0) Resource.Error(AppError.NoNetwork) else Resource.Success(listOf(Genre(28, "Action")))

    override fun moviesByGenre(genreId: Int): Flow<PagingData<Movie>> =
        flowOf(PagingData.from(listOf(Movie(1, LONG_TITLE, null, 8.2, "2022"))))
    override suspend fun getGenreBackdrops(genreId: Int): Resource<List<String>> = Resource.Success(emptyList())

    override suspend fun getMovieDetail(id: Int): Resource<MovieDetail> = Resource.Success(
        MovieDetail(
            id = id, title = LONG_TITLE, tagline = "Feel the need.", overview = "Overview text",
            posterUrl = null, backdropUrl = null, rating = 8.2, releaseDate = "2022-05-24", runtimeMinutes = 131,
            genres = listOf(Genre(28, "Action"), Genre(18, "Drama")),
        ),
    )
    /** Trailer returned for every movie; tests may set it before opening a detail screen. */
    var trailer: Video? = null

    override suspend fun getTrailer(id: Int): Resource<Video?> = Resource.Success(trailer)
    override fun reviews(movieId: Int): Flow<PagingData<Review>> {
        val done = LoadState.NotLoading(endOfPaginationReached = true)
        return flowOf(PagingData.from(emptyList(), sourceLoadStates = LoadStates(done, done, done)))
    }
}

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [RepositoryModule::class])
object FakeRepositoryModule {
    @Provides
    @Singleton
    fun provideRepository(): MovieRepository = FakeMovieRepository()
}
