package com.thasa.movietmdb.feature.movies

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thasa.movietmdb.core.domain.model.Movie
import com.thasa.movietmdb.core.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MovieListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: MovieRepository,
) : ViewModel() {

    private val genreId: Int = checkNotNull(savedStateHandle["genreId"]) { "genreId argument is required" }

    val movies: Flow<PagingData<Movie>> = repository.moviesByGenre(genreId).cachedIn(viewModelScope)
}
