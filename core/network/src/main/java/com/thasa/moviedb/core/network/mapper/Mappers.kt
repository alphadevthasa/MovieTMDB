package com.thasa.moviedb.core.network.mapper

import com.thasa.moviedb.core.domain.model.Genre
import com.thasa.moviedb.core.domain.model.Movie
import com.thasa.moviedb.core.domain.model.MovieDetail
import com.thasa.moviedb.core.domain.model.Review
import com.thasa.moviedb.core.domain.model.Video
import com.thasa.moviedb.core.network.dto.GenreDto
import com.thasa.moviedb.core.network.dto.MovieDetailDto
import com.thasa.moviedb.core.network.dto.MovieDto
import com.thasa.moviedb.core.network.dto.MovieListResponse
import com.thasa.moviedb.core.network.dto.ReviewDto
import com.thasa.moviedb.core.network.dto.VideoDto

private const val POSTER_BASE = "https://image.tmdb.org/t/p/w500"
private const val BACKDROP_BASE = "https://image.tmdb.org/t/p/w780"

private fun String?.toImageUrl(base: String): String? =
    takeUnless { it.isNullOrBlank() }?.let { base + it }

private fun String?.toYear(): String? =
    this?.takeIf { it.length >= 4 }?.take(4)

fun GenreDto.toDomain() = Genre(id = id, name = name)

fun MovieDto.toDomain() = Movie(
    id = id,
    title = title,
    posterUrl = posterPath.toImageUrl(POSTER_BASE),
    rating = voteAverage ?: 0.0,
    releaseYear = releaseDate.toYear(),
)

/** Backdrop URLs of the movies on this page that have one, in API (popularity) order. */
fun MovieListResponse.backdropUrls(): List<String> = results.mapNotNull { it.backdropPath.toImageUrl(BACKDROP_BASE) }

fun MovieDetailDto.toDomain() = MovieDetail(
    id = id,
    title = title,
    tagline = tagline?.takeIf { it.isNotBlank() },
    overview = overview?.takeIf { it.isNotBlank() },
    posterUrl = posterPath.toImageUrl(POSTER_BASE),
    backdropUrl = backdropPath.toImageUrl(BACKDROP_BASE),
    rating = voteAverage ?: 0.0,
    releaseDate = releaseDate?.takeIf { it.isNotBlank() },
    runtimeMinutes = runtime?.takeIf { it > 0 },
    genres = genres.map { it.toDomain() },
)

fun ReviewDto.toDomain() = Review(
    id = id,
    author = author,
    avatarUrl = authorDetails?.avatarPath.toAvatarUrl(),
    rating = authorDetails?.rating,
    content = content,
    createdAt = createdAt,
)

/** TMDB returns Gravatar avatars as "/https://..." and its own images as relative paths. */
private fun String?.toAvatarUrl(): String? = when {
    isNullOrBlank() -> null
    startsWith("/http") -> substring(1)
    else -> POSTER_BASE + this
}

fun List<VideoDto>.firstYouTubeTrailer(): Video? =
    firstOrNull { it.site == "YouTube" && it.type == "Trailer" }?.let { Video(it.key, it.name) }
