package com.masterbit.omdb.ui.moviedetail

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.masterbit.omdb.OMDBApp
import com.masterbit.omdb.data.Movie
import com.masterbit.omdb.databinding.MovieDetailLayoutBinding
import com.masterbit.omdb.local.MovieDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlin.coroutines.CoroutineContext

const val MOVIE_ID_KEY = "movie_id"

class MovieDetailActivity: AppCompatActivity(), CoroutineScope {

    private lateinit var binding: MovieDetailLayoutBinding

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + SupervisorJob()
    private lateinit var movieId: String
    private val movieDatabase: MovieDatabase by lazy {
        (application as OMDBApp).movieDatabase
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MovieDetailLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        intent.getStringExtra(MOVIE_ID_KEY)?.also {
            movieId = it
        } ?: run {
            Toast.makeText(this, "Movie is not provided. finishing", Toast.LENGTH_LONG).show()
            finish()
        }

        launch {
            val movie = withContext(IO) {
                movieDatabase.movieDao().queryMovieById(movieId)
            }

            bindTo(movie)
        }
    }

    private fun bindTo(movie: Movie) {
        val glideUrl = GlideUrl(movie.posterUrl)
        Glide.with(binding.root).load(glideUrl).into(binding.moviePoster)
        if (movie.actors.isNullOrEmpty() && movie.director.isNullOrEmpty()) {
            binding.cast.text = "N/A"
        } else {
            binding.cast.text = "Cast: ${movie.actors + if (!movie.director.isNullOrEmpty()) ", " + movie.director else ""}"
        }

        binding.overview.text = "Overview: ${movie.plot}"
        binding.duration.text = "Runtime: ${movie.runtime}"
        binding.movieRating.text = "Rating: ${movie.rating}"
        binding.movieTitle.text = movie.title
        binding.releaseYear.text = "Release: ${movie.releasedYear}"
    }
}