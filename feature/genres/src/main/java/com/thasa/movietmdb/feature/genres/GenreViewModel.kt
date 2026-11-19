package com.thasa.movietmdb.feature.genres

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thasa.movietmdb.core.domain.AppError
import com.thasa.movietmdb.core.domain.Resource
import com.thasa.movietmdb.core.domain.model.Genre
import com.thasa.movietmdb.core.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@HiltViewModel
class GenreViewModel @Inject constructor(
    private val repository: MovieRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<Resource<List<Genre>>>(Resource.Loading)
    val state: StateFlow<Resource<List<Genre>>> = _state.asStateFlow()

    private val _backdrops = MutableStateFlow<Map<Int, String>>(emptyMap())

    /** Genre id → backdrop URL; a genre without an entry is shown without an image. */
    val backdrops: StateFlow<Map<Int, String>> = _backdrops.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        loadJob?.cancel()
        _backdrops.value = emptyMap()
        loadJob = viewModelScope.launch {
            _state.value = Resource.Loading
            val result = repository.getGenres()
            _state.value = result
            if (result is Resource.Success) loadBackdrops(result.data)
        }
    }

    private suspend fun loadBackdrops(genres: List<Genre>) {
        // Child coroutines: cancelled together with loadJob. A slow genre is dropped after the timeout
        // so it cannot keep every card blank.
        val candidates = coroutineScope {
            genres.map { genre ->
                async {
                    val result = withTimeoutOrNull(BACKDROP_TIMEOUT_MS) { repository.getGenreBackdrops(genre.id) }
                    genre.id to (result ?: Resource.Error(AppError.Timeout))
                }
            }.awaitAll()
        }.toMap()
        _backdrops.value = assignUnique(genres, candidates)
    }

    /**
     * Gives each genre (in list order) its first candidate not used by an earlier genre,
     * so one very popular movie doesn't repeat across cards. Falls back to the first candidate.
     */
    private fun assignUnique(genres: List<Genre>, candidates: Map<Int, Resource<List<String>>>): Map<Int, String> {
        val used = mutableSetOf<String>()
        val assigned = linkedMapOf<Int, String>()
        for (genre in genres) {
            val urls = (candidates[genre.id] as? Resource.Success)?.data.orEmpty()
            val pick = urls.firstOrNull { it !in used } ?: urls.firstOrNull() ?: continue
            used += pick
            assigned[genre.id] = pick
        }
        return assigned
    }

    private companion object {
        const val BACKDROP_TIMEOUT_MS = 5_000L
    }
}
