package com.thasa.moviedb.core.domain.model

data class MovieDetail(
    val id: Int,
    val title: String,
    val tagline: String?,
    val overview: String?,
    val posterUrl: String?,
    val backdropUrl: String?,
    val rating: Double,
    val releaseDate: String?,
    val runtimeMinutes: Int?,
    val genres: List<Genre>,
)
