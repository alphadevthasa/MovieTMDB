package com.thasa.moviedb.core.network.api

import com.thasa.moviedb.core.network.dto.GenreListResponse
import com.thasa.moviedb.core.network.dto.MovieDetailDto
import com.thasa.moviedb.core.network.dto.MovieListResponse
import com.thasa.moviedb.core.network.dto.ReviewListResponse
import com.thasa.moviedb.core.network.dto.VideoListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {
    @GET("genre/movie/list")
    suspend fun getGenres(): GenreListResponse

    @GET("discover/movie")
    suspend fun discover(
        @Query("with_genres") genreId: Int,
        @Query("page") page: Int,
    ): MovieListResponse

    @GET("movie/{id}")
    suspend fun movieDetail(@Path("id") id: Int): MovieDetailDto

    @GET("movie/{id}/videos")
    suspend fun videos(@Path("id") id: Int): VideoListResponse

    @GET("movie/{id}/reviews")
    suspend fun reviews(
        @Path("id") id: Int,
        @Query("page") page: Int,
    ): ReviewListResponse
}
