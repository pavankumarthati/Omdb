package com.masterbit.omdb.local

import androidx.room.*
import com.masterbit.omdb.data.Movie
import com.masterbit.omdb.data.MovieItem
import io.reactivex.Observable
import io.reactivex.Single


@Dao
interface MovieDao {
    @Query("SELECT * FROM movie")
    fun queryMovies(): Single<List<Movie>>

    @Query("SELECT * FROM movie WHERE imdbId=:movieId")
    fun queryMovieById(movieId: String): Observable<List<Movie>>

    @Query("SELECT * FROM movie WHERE title LIKE '%' || :title || '%' AND releasedYear LIKE '%' || :year || '%'")
    fun queryMoviesByTitleAndYear(title: String, year: String): Observable<List<Movie>>

    @Query("SELECT * FROM movie WHERE title LIKE '%' || :title || '%'")
    fun queryMoviesByTitle(title: String): Observable<List<Movie>>

    @Query("SELECT * FROM movie WHERE favorite = 1")
    fun queryFavoriteMovies(): Observable<List<Movie>>

//    @Query("SELECT * FROM movie WHERE lower(country)=lower(:country)")
//    fun queryPopulationCities(country: String): Single<List<PopulationEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMovies(movies: List<Movie>): List<Long>

    @Transaction
    fun upsert(movies: List<Movie>) {
        val insertResult: List<Long> = insertMovies(movies)
        val updateList: MutableList<Movie> = ArrayList()
        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) {
                updateList.add(movies[i])
            }
        }
        if (updateList.isNotEmpty()) {
            update(updateList.map { MovieItem.fromMovie(it) })
        }
    }

    @Update(entity = Movie::class)
    fun update(obj: List<MovieItem>)

    @Query("UPDATE movie SET favorite = :favorite WHERE imdbId=:id")
    fun updateFavorite(id: String, favorite: Boolean)

    @Query("DELETE FROM movie")
    fun deleteAll()
}