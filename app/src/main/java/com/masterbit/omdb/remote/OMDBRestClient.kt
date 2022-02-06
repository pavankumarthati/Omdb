package com.masterbit.omdb.remote

import com.masterbit.omdb.BuildConfig
import com.masterbit.omdb.data.MovieDetailResponse
import com.masterbit.omdb.data.MovieSearchResultResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


interface OMDBRestClient {

    @GET("?apikey=${BuildConfig.API_KEY}")
    fun getMoviesByTitleAndYear(@Query("s") title: String, @Query("y") year: String): Observable<MovieSearchResultResponse>

    @GET("?apikey=${BuildConfig.API_KEY}")
    fun getMovieById(@Query("i") id: String): Observable<MovieDetailResponse>

    companion object {
        fun getOmdbRestClient(): OMDBRestClient {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl("https://www.omdbapi.com/")
                .client(OkHttpClient.Builder().addInterceptor(interceptor).build())
                .build().create(OMDBRestClient::class.java)
        }


    }
}