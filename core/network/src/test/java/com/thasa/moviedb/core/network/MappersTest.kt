package com.thasa.moviedb.core.network

import com.thasa.moviedb.core.network.dto.AuthorDetailsDto
import com.thasa.moviedb.core.network.dto.GenreDto
import com.thasa.moviedb.core.network.dto.MovieDetailDto
import com.thasa.moviedb.core.network.dto.MovieDto
import com.thasa.moviedb.core.network.dto.ReviewDto
import com.thasa.moviedb.core.network.dto.VideoDto
import com.thasa.moviedb.core.network.mapper.firstYouTubeTrailer
import com.thasa.moviedb.core.network.mapper.toDomain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MappersTest {

    private fun movieDto(poster: String? = "/p.jpg", date: String? = "2023-05-01") =
        MovieDto(id = 1, title = "T", posterPath = poster, voteAverage = 7.5, releaseDate = date)

    @Test
    fun movieDto_withNullPoster_mapsToNullPosterUrl() {
        assertNull(movieDto(poster = null).toDomain().posterUrl)
    }

    @Test
    fun movieDto_withPoster_buildsFullUrl() {
        assertEquals("https://image.tmdb.org/t/p/w500/p.jpg", movieDto().toDomain().posterUrl)
    }

    @Test
    fun movieDto_releaseDate_2023_05_01_mapsToYear_2023() {
        assertEquals("2023", movieDto().toDomain().releaseYear)
    }

    @Test
    fun movieDto_blankReleaseDate_mapsToNullYear() {
        assertNull(movieDto(date = "").toDomain().releaseYear)
        assertNull(movieDto(date = null).toDomain().releaseYear)
    }

    @Test
    fun detailDto_nullOverview_mapsToNull() {
        val dto = MovieDetailDto(
            id = 1, title = "T", tagline = null, overview = null, posterPath = null,
            backdropPath = "/b.jpg", voteAverage = null, releaseDate = null, runtime = null,
            genres = listOf(GenreDto(28, "Action")),
        )
        val domain = dto.toDomain()
        assertNull(domain.overview)
        assertNull(domain.posterUrl)
        assertEquals("https://image.tmdb.org/t/p/w780/b.jpg", domain.backdropUrl)
        assertEquals(0.0, domain.rating, 0.0)
        assertEquals("Action", domain.genres.single().name)
    }

    @Test
    fun reviewDto_avatarWithLeadingSlashHttp_stripsSlash() {
        val dto = ReviewDto(
            id = "r", author = "a", content = "c", createdAt = null,
            authorDetails = AuthorDetailsDto(avatarPath = "/https://www.gravatar.com/avatar/x", rating = 8.0),
        )
        assertEquals("https://www.gravatar.com/avatar/x", dto.toDomain().avatarUrl)
    }

    @Test
    fun reviewDto_relativeAvatar_usesImageBase() {
        val dto = ReviewDto("r", "a", AuthorDetailsDto("/a.jpg", null), "c", null)
        assertEquals("https://image.tmdb.org/t/p/w500/a.jpg", dto.toDomain().avatarUrl)
    }

    @Test
    fun reviewDto_nullRating_mapsToNull() {
        val dto = ReviewDto("r", "a", AuthorDetailsDto(null, null), "c", null)
        assertNull(dto.toDomain().rating)
        assertNull(dto.toDomain().avatarUrl)
    }

    @Test
    fun reviewDto_nullAuthorDetails_doesNotCrash() {
        val dto = ReviewDto("r", "a", null, "c", null)
        assertNull(dto.toDomain().rating)
    }

    @Test
    fun videoList_picksFirstYouTubeTrailer() {
        val videos = listOf(
            VideoDto("v1", "Teaser", "YouTube", "Teaser"),
            VideoDto("v2", "Trailer", "YouTube", "Trailer"),
            VideoDto("v3", "Trailer 2", "YouTube", "Trailer"),
        )
        assertEquals("v2", videos.firstYouTubeTrailer()?.key)
    }

    @Test
    fun videoList_onlyVimeoOrTeaser_returnsNull() {
        val videos = listOf(
            VideoDto("v1", "Trailer", "Vimeo", "Trailer"),
            VideoDto("v2", "Teaser", "YouTube", "Teaser"),
        )
        assertNull(videos.firstYouTubeTrailer())
    }
}
