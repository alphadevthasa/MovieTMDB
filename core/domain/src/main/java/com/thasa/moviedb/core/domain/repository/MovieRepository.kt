package com.thasa.moviedb.core.domain.repository

import androidx.paging.PagingData
import com.thasa.moviedb.core.domain.Resource
import com.thasa.moviedb.core.domain.model.Genre
import com.thasa.moviedb.core.domain.model.Movie
import com.thasa.moviedb.core.domain.model.MovieDetail
import com.thasa.moviedb.core.domain.model.Review
import com.thasa.moviedb.core.domain.model.Video
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    suspend fun getGenres(): Resource<List<Genre>>
    fun moviesByGenre(genreId: Int): Flow<PagingData<Movie>>

    /** Backdrop image URLs of popular movies in the genre, most popular first. */
    suspend fun getGenreBackdrops(genreId: Int): Resource<List<String>>
    suspend fun getMovieDetail(id: Int): Resource<MovieDetail>
    suspend fun getTrailer(id: Int): Resource<Video?>
    fun reviews(movieId: Int): Flow<PagingData<Review>>
}
