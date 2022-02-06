package com.masterbit.omdb.ui.moviedetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.masterbit.omdb.data.Movie
import com.masterbit.omdb.local.MovieDatabase
import io.reactivex.disposables.CompositeDisposable

class MovieDetailViewModel(private val database: MovieDatabase) : ViewModel() {

    private val subscription = CompositeDisposable()
    private val _movieLiveData = MutableLiveData<Movie>()
    val movieLiveData: LiveData<Movie> = _movieLiveData

    fun fetchMoviesById(movieId: String) {
        subscription.add(
            database.movieDao().queryMovieById(movieId)
            .subscribe {
                _movieLiveData.postValue(it[0])
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        if (!subscription.isDisposed) {
            subscription.dispose()
        }
    }
}