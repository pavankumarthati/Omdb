package com.masterbit.omdb.ui.dashboard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masterbit.omdb.data.Movie
import com.masterbit.omdb.local.MovieDatabase
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoriteViewModel(private val database: MovieDatabase) : ViewModel() {

    private val subscription = CompositeDisposable()
    private val _favoriteMovies = MutableLiveData<List<Movie>>()
    val favoriteMovies = _favoriteMovies

    fun fetchFavoriteMovies() {
        subscription.add(database.movieDao().queryFavoriteMovies().subscribe {
            _favoriteMovies.postValue(it)
        })
    }

    fun updateFavorite(movieId: String, isFavorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            database.movieDao().updateFavorite(movieId, isFavorite)
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (!subscription.isDisposed) {
            subscription.dispose()
        }
    }
}