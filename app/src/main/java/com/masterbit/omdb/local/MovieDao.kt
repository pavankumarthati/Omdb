package com.masterbit.omdb.local

import androidx.room.*
import com.masterbit.omdb.data.Movie
import io.reactivex.Single

@Dao
interface MovieDao {
    @Query("SELECT * FROM movie")
    fun queryMovies(): Single<List<Movie>>

    @Query("SELECT * FROM movie WHERE imdbId=:movieId")
    fun queryMovieById(movieId: String): Movie

//    @Query("SELECT * FROM movie WHERE lower(country)=lower(:country)")
//    fun queryPopulationCities(country: String): Single<List<PopulationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMovies(movies: List<Movie>)

    @Query("UPDATE movie SET favorite = :favorite WHERE imdbId=:id")
    fun updateFavorite(id: String, favorite: Boolean)

    @Query("DELETE FROM movie")
    fun deleteAll()
}