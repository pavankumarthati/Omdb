package com.masterbit.omdb.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jakewharton.rxbinding3.InitialValueObservable
import com.masterbit.omdb.data.Data
import com.masterbit.omdb.data.Movie
import com.masterbit.omdb.data.MovieDetailResponse
import com.masterbit.omdb.data.MovieSearchResultItem
import com.masterbit.omdb.local.MovieDatabase
import com.masterbit.omdb.remote.OMDBRestClient
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(private val movieDatabase: MovieDatabase) : ViewModel() {

    private val _errorLiveData = MutableLiveData<String>()
    val errorLiveData = _errorLiveData
    private val _searchBtnEnableLiveData = MutableLiveData<Boolean>(false)
    val searchBtnEnableLiveData: LiveData<Boolean> = _searchBtnEnableLiveData
    private val subscription = CompositeDisposable()
    private val omdbRestClient = OMDBRestClient.getOmdbRestClient()
    private val _progressLiveData = MutableLiveData<Boolean>(false)
    val progressLiveData: LiveData<Boolean> = _progressLiveData

    private val _movieResultsLiveData = MutableLiveData<List<Movie>>()
    val movieResultsLiveData: LiveData<List<Movie>> = _movieResultsLiveData


    fun search(title: String, year: String, movieId: String) {
        subscription.clear()
        subscription.add(
            Observable.merge(
                fetchMoviesFromDB(title, year, movieId).subscribeOn(Schedulers.io()),
                fetchMoviesFromNetwork(title, year, movieId).subscribeOn(Schedulers.io())
            ).observeOn(AndroidSchedulers.mainThread())
                .startWith(Data.Loading)
            .subscribe({
                _progressLiveData.value = false
                when (it) {
                    is Data.Loading -> {
                        _progressLiveData.value = true
                    }
                    is Data.Success<*> -> {
                        _movieResultsLiveData.value = it.data as List<Movie>
                    }
                    is Data.Error -> {
                        _errorLiveData.value = it.msg
                    }
                }
            }, {
                _progressLiveData.value = false
                _errorLiveData.value = "Please check your network connection"
                it.printStackTrace()
            }, {
                println("stream is closed properly")
            })
        )
    }

    private fun fetchMoviesFromDB(title: String, year: String, movieId: String): Observable<out Data> {
        return if (title.isNullOrEmpty() && movieId.isNullOrEmpty()) {
            Observable.just(Data.Error(msg = "title or movie id is not provided"))
        } else {
            when {
                movieId.isNotEmpty() -> {
                    movieDatabase.movieDao().queryMovieById(movieId)
                }
                year.isNotEmpty() -> {
                    movieDatabase.movieDao().queryMoviesByTitleAndYear(title, year)
                }
                else -> {
                    movieDatabase.movieDao().queryMoviesByTitle(title)
                }
            }.map {
                Data.Success(it)
            }
        }
    }

    private fun fetchMoviesFromNetwork(title: String, year: String, movieId: String): Observable<out Data> {
        return when {
            title.isNullOrEmpty() && movieId.isNullOrEmpty() -> {
                Observable.just(Data.Error(msg = "title or movie id is not provided"))
            }
            movieId.isNotEmpty() -> {
                omdbRestClient.getMovieById(movieId).map { movieDetailResponse ->
                    if (movieDetailResponse.response.contentEquals(
                            "false",
                            true
                        ) || movieDetailResponse.error?.isNotEmpty() == true
                    ) {
                        Data.Error(movieDetailResponse.error!!)
                    } else {
                        Data.Success(listOf(movieDetailResponse.toMovie()))
                    }
                }
            }
            else -> {
                omdbRestClient.getMoviesByTitleAndYear(title, year)
                    .map { movieSearchResults ->
                        if (movieSearchResults.response.contentEquals(
                                "false",
                                true
                            ) || movieSearchResults.error?.isNotEmpty() == true
                        ) {
                            Data.Error(movieSearchResults.error!!)
                        } else {
                            Data.Success(movieSearchResults.search!!)
                        }
                    }
                    .publish {
                        Observable.merge(
                            it.ofType(Data.Error::class.java),
                            it.ofType(Data.Success::class.java)
                                .compose(ObservableTransformer<Data.Success<*>, Data> {
                                    it.singleElement().flattenAsObservable {
                                        it.data
                                    }.flatMap { movieSearchResultItem ->
                                        omdbRestClient.getMovieById((movieSearchResultItem as MovieSearchResultItem).imdbId)
                                            .onErrorReturn {
                                                MovieDetailResponse(
                                                    error = it.localizedMessage,
                                                    response = "false"
                                                )
                                            }
                                    }.filter {
                                        !(it.response.contentEquals(
                                            "false",
                                            true
                                        ) || it.error?.isNotEmpty() == true)
                                    }.map {
                                        it.toMovie()
                                    }.toList().toObservable().map { Data.Success(it) }
                                })
                        )
                    }.publish().refCount()
            }
        }
        .doOnNext {
            movieDatabase.runInTransaction {
                when (it) {
                    is Data.Success<*> -> {
                        movieDatabase.movieDao().upsert(it.data as List<Movie>)
                    }
                    else -> {
                        // No need to handle
                    }
                }
            }
        }
    }

    fun updateFavorite(movieId: String, isFavorite: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            movieDatabase.movieDao().updateFavorite(movieId, isFavorite)
        }
    }

    fun trackSearchBtnEnable(
        tf1: InitialValueObservable<CharSequence>,
        tf2: InitialValueObservable<CharSequence>
    ) {
        Observable.combineLatest(tf1, tf2) { t1, t2 ->
            _searchBtnEnableLiveData.value = t1.isNotEmpty() || t2.isNotEmpty()
        }.subscribe()
    }
}