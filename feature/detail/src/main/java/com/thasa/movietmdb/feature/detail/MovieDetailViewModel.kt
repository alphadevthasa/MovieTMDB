package com.thasa.movietmdb.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.thasa.movietmdb.core.domain.Resource
import com.thasa.movietmdb.core.domain.model.MovieDetail
import com.thasa.movietmdb.core.domain.model.Review
import com.thasa.movietmdb.core.domain.model.Video
import com.thasa.movietmdb.core.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MovieRepository,
) : ViewModel() {

    private val movieId: Int = checkNotNull(savedStateHandle["movieId"]) { "movieId argument is required" }

    private val _detail = MutableStateFlow<Resource<MovieDetail>>(Resource.Loading)
    val detail: StateFlow<Resource<MovieDetail>> = _detail.asStateFlow()

    private val _trailer = MutableStateFlow<Resource<Video?>>(Resource.Loading)
    val trailer: StateFlow<Resource<Video?>> = _trailer.asStateFlow()

    val reviews: Flow<PagingData<Review>> = repository.reviews(movieId).cachedIn(viewModelScope)

    init {
        retry()
    }

    /** Reloads the detail and the trailer. */
    fun retry() {
        loadDetail()
        retryTrailer()
    }

    fun retryTrailer() {
        viewModelScope.launch {
            _trailer.value = Resource.Loading
            _trailer.value = repository.getTrailer(movieId)
        }
    }

    private fun loadDetail() {
        viewModelScope.launch {
            _detail.value = Resource.Loading
            _detail.value = repository.getMovieDetail(movieId)
        }
    }
}
