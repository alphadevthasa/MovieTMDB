package com.thasa.movietmdb.core.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.thasa.movietmdb.core.domain.model.Movie
import com.thasa.movietmdb.core.network.api.TmdbApi
import com.thasa.movietmdb.core.data.mapper.toDomain
import com.thasa.movietmdb.core.data.toAppError
import kotlin.coroutines.cancellation.CancellationException

class MoviePagingSource(
    private val api: TmdbApi,
    private val genreId: Int,
) : PagingSource<Int, Movie>() {

    /** TMDB discover can repeat a movie on a later page; the UI list needs unique ids. */
    private val seenIds = mutableSetOf<Int>()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: FIRST_PAGE
        return try {
            val response = api.discover(genreId, page)
            LoadResult.Page(
                data = response.results.map { it.toDomain() }.filter { seenIds.add(it.id) },
                prevKey = null,
                nextKey = if (page < response.totalPages) page + 1 else null,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoadResult.Error(e.toAppError())
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? = null

    private companion object {
        const val FIRST_PAGE = 1
    }
}
