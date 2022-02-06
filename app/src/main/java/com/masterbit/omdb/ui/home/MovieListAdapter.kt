package com.masterbit.omdb.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.masterbit.omdb.data.Movie
import com.masterbit.omdb.databinding.MovieItemRvBinding

class MovieListAdapter(private val goToDetailPage: (movieId: String) -> Unit, private val markMovieFavoriteAction: (movieId: String, isChecked: Boolean) -> Unit): ListAdapter<Movie, MovieItemViewHolder>(MovieItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieItemViewHolder {
        return MovieItemViewHolder.create(parent, goToDetailPage, markMovieFavoriteAction)
    }

    override fun onBindViewHolder(holder: MovieItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class MovieItemViewHolder(private val binding: MovieItemRvBinding, private val clickAction: (movieId: String) -> Unit, private val markMovieFavoriteAction: (movieId: String, isChecked: Boolean) -> Unit): RecyclerView.ViewHolder(binding.root) {

    fun bind(movie: Movie) {
        val glideUrl = GlideUrl(movie.posterUrl)
        Glide.with(binding.root).load(glideUrl).into(binding.moviePoster)
        binding.movieName.text = movie.title
        binding.movieDirector.text = movie.director
        binding.moviePlot.text = movie.plot
        binding.movieYear.text = movie.releasedYear
        binding.root.tag = movie.imdbId
        binding.root.setOnClickListener(clickListener)
        binding.movieFavorite.setOnCheckedChangeListener(null)
        binding.movieFavorite.isChecked = movie.favorite
        binding.movieFavorite.tag = movie.imdbId
        binding.movieFavorite.setOnCheckedChangeListener(markMovieFavoriteClickListener)
    }

    private val clickListener = View.OnClickListener {
        clickAction.invoke(it.tag as String)
    }

    private val markMovieFavoriteClickListener = CompoundButton.OnCheckedChangeListener { btn, checked -> markMovieFavoriteAction.invoke(btn.tag as String, checked) }

    companion object {
        fun create(parent: ViewGroup, clickAction: (movieId: String) -> Unit, markMovieFavoriteAction: (movieId: String, isChecked: Boolean) -> Unit): MovieItemViewHolder {
            return MovieItemViewHolder(MovieItemRvBinding.inflate(LayoutInflater.from(parent.context), parent, false), clickAction, markMovieFavoriteAction)
        }
    }
}

class MovieItemDiffCallback: DiffUtil.ItemCallback<Movie>() {

    override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
        return oldItem.imdbId == newItem.imdbId
    }

}