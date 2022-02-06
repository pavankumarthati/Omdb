package com.masterbit.omdb.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.masterbit.omdb.data.Data
import com.masterbit.omdb.data.Movie
import com.masterbit.omdb.data.MovieDetailResponse
import com.masterbit.omdb.data.MovieSearchResultItem
import com.masterbit.omdb.local.MovieDatabase
import com.masterbit.omdb.remote.OMDBRestClient
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(private val movieDatabase: MovieDatabase) : ViewModel() {

    private val subscription = CompositeDisposable()
    private val omdbRestClient = OMDBRestClient.getOmdbRestClient()
    private val _progressLiveData = MutableLiveData<Boolean>(false)
    val progressLiveData: LiveData<Boolean> = _progressLiveData

    private val _movieResultsLiveData = MutableLiveData<List<Movie>>()
    val movieResultsLiveData: LiveData<List<Movie>> = _movieResultsLiveData


    fun search(title: String, year: String, movieId: String) {
//        subscription.clear()
        _progressLiveData.value = true
        subscription.add(
            if (title.isNullOrEmpty() && movieId.isNullOrEmpty()) {
                Observable.just(Data.Error(msg = "title or movie id is not provided"))
            } else {
                fetchMoviesFromNetwork(title, year, movieId)
            }
            .doOnNext {
                movieDatabase.runInTransaction {
                    when (it) {
                        is Data.Success<*> -> {
                            movieDatabase.movieDao().insertMovies(it.data as List<Movie>)
                        }
                        else -> {
                            // No need to handle
                        }
                    }
                }
            }
            .subscribeOn(Schedulers.io())
            .subscribe({
                _progressLiveData.postValue(false)
                when (it) {
                    is Data.Success<*> -> {
                        _movieResultsLiveData.postValue(it.data as List<Movie>)
                    }
                    is Data.Error -> {
//                _errorLiveData.postValue(it)
                    }
                }
            }, {
                println("This is serious problem..")
                it.printStackTrace()
            })
        )
    }

    private fun fetchMoviesFromNetwork(title: String, year: String, movieId: String): Observable<Data> {
        return if (movieId.isNotEmpty()) {
            omdbRestClient.getMovieById(movieId).map { movieDetailResponse ->
                if (movieDetailResponse.response.contentEquals("false", true) || movieDetailResponse.error?.isNotEmpty() == true) {
                    Data.Error(movieDetailResponse.error!!)
                } else {
                    Data.Success(listOf(movieDetailResponse.toMovie()))
                }
            }
        } else  {
            omdbRestClient.getMoviesByTitleAndYear(title, year)
                .map { movieSearchResults ->
                        if (movieSearchResults.response.contentEquals("false", true) || movieSearchResults.error?.isNotEmpty() == true) {
                            Data.Error(movieSearchResults.error!!)
                        } else {
                            Data.Success(movieSearchResults.search!!)
                        }
                    }
                .publish {
                    Observable.merge(it.ofType(Data.Error::class.java),
                        it.ofType(Data.Success::class.java)
                            .compose(ObservableTransformer<Data.Success<*>, Data> {
                                it.singleOrError().flattenAsObservable {
                                    it.data
                                }.flatMap { movieSearchResultItem ->
                                    omdbRestClient.getMovieById((movieSearchResultItem as MovieSearchResultItem).imdbId).onErrorReturn {
                                        MovieDetailResponse(error = it.localizedMessage, response = "false")
                                    }
                                }.filter {
                                    !(it.response.contentEquals("false", true) || it.error?.isNotEmpty() == true)
                                }.map {
                                    it.toMovie()
                                }.toList().toObservable().map { Data.Success(it) }
                            })
                    )
                }
            }
        }

    fun updateFavorite(movieId: String, isFavorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            movieDatabase.movieDao().updateFavorite(movieId, isFavorite)
        }
    }
}