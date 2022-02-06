package com.masterbit.omdb.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "movie")
data class Movie(
     val title: String, val releasedYear: String? = null,
     val runtime: String, val genre: String? = null,
     val director: String? = null, val writer: String? = null,
     val actors: String? = null, val plot: String? = null,
     val language: String? = null, val country: String? = null,
     val posterUrl: String? = null, val rating: String? = null,
     @PrimaryKey(autoGenerate = false)
     val imdbId: String,
     val favorite: Boolean = false,
) {
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Movie -> {
                this.imdbId == other.imdbId
            }
            else -> false
        }
    }

    override fun hashCode(): Int {
        return imdbId.hashCode()
    }
}

data class MovieItem(
    val title: String, val releasedYear: String? = null,
    val runtime: String, val genre: String? = null,
    val director: String? = null, val writer: String? = null,
    val actors: String? = null, val plot: String? = null,
    val language: String? = null, val country: String? = null,
    val posterUrl: String? = null, val rating: String? = null,
    val imdbId: String
) {
    companion object {
        fun fromMovie(movie: Movie): MovieItem {
            return MovieItem(
                title = movie.title,
                releasedYear = movie.releasedYear,
                runtime = movie.runtime,
                genre = movie.genre,
                director = movie.director,
                writer = movie.writer,
                actors = movie.actors,
                plot = movie.plot,
                language = movie.language,
                country = movie.country,
                posterUrl = movie.posterUrl,
                rating = movie.rating,
                imdbId = movie.imdbId
            )
        }
    }
}

data class MovieSearchResultResponse(
                @SerializedName(value = "Search")
                val search: List<MovieSearchResultItem>? = null,
                @SerializedName(value = "Error")
                val error: String? = null,
                @SerializedName(value = "response", alternate = ["Response"])
                val response: String
)

data class MovieSearchResultItem(
            @SerializedName(value = "Title")
            val title: String,
            @SerializedName(value = "imdbID")
            val imdbId: String
)

data class MovieDetailResponse(
    @SerializedName(value = "Title")
    val title: String? = null,
    @SerializedName(value = "Released")
    val releasedYear: String? = null,
    @SerializedName(value = "Runtime")
    val runtime: String? = null,
    @SerializedName(value = "Genre")
    val genre: String? = null,
    @SerializedName(value = "Director")
    val director: String? = null,
    @SerializedName(value = "Writer")
    val writer: String? = null,
    @SerializedName(value = "Actors")
    val actors: String? = null,
    @SerializedName(value = "Plot")
    val plot: String? = null,
    @SerializedName(value = "Language")
    val language: String? = null,
    @SerializedName(value = "Country")
    val country: String? = null,
    @SerializedName(value = "Poster")
    val posterUrl: String? = null,
    @SerializedName(value = "imdbRating")
    val rating: String? = null,
    @SerializedName(value = "imdbID")
    val imdbId: String? = null,
    val favorite: Boolean = false,
    @SerializedName(value = "Response")
    val response: String,
    @SerializedName(value = "Error")
    val error: String? = null
) {
    fun toMovie(): Movie {
        return Movie(title!!, releasedYear, runtime!!, genre, director, writer, actors, plot, language, country, posterUrl, rating, imdbId!!, favorite)
    }
}