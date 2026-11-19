package com.thasa.moviedb

import androidx.paging.PagingData
import com.thasa.moviedb.core.domain.AppError
import com.thasa.moviedb.core.domain.Resource
import com.thasa.moviedb.core.domain.model.Genre
import com.thasa.moviedb.core.domain.model.Movie
import com.thasa.moviedb.core.domain.model.MovieDetail
import com.thasa.moviedb.core.domain.model.Review
import com.thasa.moviedb.core.domain.model.Video
import com.thasa.moviedb.core.domain.repository.MovieRepository
import com.thasa.moviedb.core.network.di.RepositoryModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Singleton

/** Fails the first genre request with [AppError.NoNetwork], then succeeds. */
class FakeMovieRepository : MovieRepository {
    private var genreCalls = 0

    override suspend fun getGenres(): Resource<List<Genre>> =
        if (genreCalls++ == 0) Resource.Error(AppError.NoNetwork) else Resource.Success(listOf(Genre(28, "Action")))

    override fun moviesByGenre(genreId: Int): Flow<PagingData<Movie>> = flowOf(PagingData.empty())
    override suspend fun getMovieDetail(id: Int): Resource<MovieDetail> = Resource.Error(AppError.NotFound)
    override suspend fun getTrailer(id: Int): Resource<Video?> = Resource.Success(null)
    override fun reviews(movieId: Int): Flow<PagingData<Review>> = flowOf(PagingData.empty())
}

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [RepositoryModule::class])
object FakeRepositoryModule {
    @Provides
    @Singleton
    fun provideRepository(): MovieRepository = FakeMovieRepository()
}
