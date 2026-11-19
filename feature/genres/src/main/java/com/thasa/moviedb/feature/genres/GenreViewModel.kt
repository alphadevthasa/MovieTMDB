package com.thasa.moviedb.feature.genres

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thasa.moviedb.core.domain.Resource
import com.thasa.moviedb.core.domain.model.Genre
import com.thasa.moviedb.core.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GenreViewModel @Inject constructor(
    private val repository: MovieRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<Resource<List<Genre>>>(Resource.Loading)
    val state: StateFlow<Resource<List<Genre>>> = _state.asStateFlow()

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        viewModelScope.launch {
            _state.value = Resource.Loading
            _state.value = repository.getGenres()
        }
    }
}
