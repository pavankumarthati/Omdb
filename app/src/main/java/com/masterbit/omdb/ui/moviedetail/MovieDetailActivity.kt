package com.masterbit.omdb.ui.moviedetail

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.masterbit.omdb.OMDBApp
import com.masterbit.omdb.data.Movie
import com.masterbit.omdb.databinding.MovieDetailLayoutBinding
import com.masterbit.omdb.local.MovieDatabase


const val MOVIE_ID_KEY = "movie_id"

class MovieDetailActivity: AppCompatActivity() {

    private val movieDetailViewModel by viewModels<MovieDetailViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MovieDetailViewModel((application as OMDBApp).movieDatabase) as T
            }

        }
    }
    private lateinit var binding: MovieDetailLayoutBinding

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
        movieDetailViewModel.movieLiveData.observe(this, ::bindTo)
        movieDetailViewModel.fetchMoviesById(movieId)
    }

    private fun bindTo(movie: Movie) {
        val glideUrl = GlideUrl(movie.posterUrl)
        Glide.with(binding.root).load(glideUrl).into(binding.moviePoster)
        if (movie.actors.isNullOrEmpty() && movie.director.isNullOrEmpty()) {
            binding.cast.text = "N/A"
        } else {
            val cast = "Cast: ${movie.actors + if (!movie.director.isNullOrEmpty()) ", " + movie.director else ""}"
            binding.cast.setText(getSpannableString(cast, cast.indexOf("Cast"), "Cast".length), TextView.BufferType.SPANNABLE)
        }

        val overview = "Overview: ${movie.plot}"
        binding.overview.setText(getSpannableString(overview, overview.indexOf("Overview"), "Overview".length), TextView.BufferType.SPANNABLE)
        val duration = "Duration: ${movie.runtime}"
        binding.duration.setText(getSpannableString(duration, duration.indexOf("Duration"), "Duration".length), TextView.BufferType.SPANNABLE)
        val rating = "Rating: ${movie.rating}"
        binding.movieRating.setText(getSpannableString(rating, rating.indexOf("Rating"), "Rating".length), TextView.BufferType.SPANNABLE)
        binding.movieTitle.text = movie.title
        val release = "Release: ${movie.releasedYear}"
        binding.releaseYear.setText(getSpannableString(release, release.indexOf("Release"), "Release".length), TextView.BufferType.SPANNABLE)

        if (supportActionBar != null) {
            supportActionBar!!.title = movie.title
        }
    }

    private fun getSpannableString(text: String, start: Int, length: Int): Spannable {
        val sb: Spannable = SpannableString(text)
        sb.setSpan(
            StyleSpan(Typeface.BOLD),
            start , length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return sb
    }
}