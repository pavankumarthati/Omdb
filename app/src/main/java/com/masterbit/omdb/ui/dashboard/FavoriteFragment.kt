package com.masterbit.omdb.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.masterbit.omdb.OMDBApp
import com.masterbit.omdb.data.Movie
import com.masterbit.omdb.databinding.FragmentFavoriteBinding
import com.masterbit.omdb.ui.home.MovieListAdapter
import com.masterbit.omdb.ui.moviedetail.MOVIE_ID_KEY
import com.masterbit.omdb.ui.moviedetail.MovieDetailActivity

class FavoriteFragment : Fragment() {

    private val favoriteViewModel by viewModels<FavoriteViewModel> {
        object: ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FavoriteViewModel((requireActivity().application as OMDBApp).movieDatabase) as T
            }

        }
    }
    private var _binding: FragmentFavoriteBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        favoriteViewModel.fetchFavoriteMovies()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        binding.favoriteMovies.adapter = MovieListAdapter(
            {
                startActivity(Intent(this.requireContext(), MovieDetailActivity::class.java).apply {
                    putExtras(bundleOf(MOVIE_ID_KEY to it))
                })
            },
            {
                movieId, isChecked -> favoriteViewModel.updateFavorite(movieId, isChecked)
            }
        )
        favoriteViewModel.favoriteMovies.observe(this.viewLifecycleOwner) {
            binding.noFavorite.isVisible = it.isEmpty()
            bindTo(it)
        }
        return binding.root
    }

    private fun bindTo(movies: List<Movie>) {
        binding.favoriteMovies.isVisible = true
        (binding.favoriteMovies.adapter as MovieListAdapter).submitList(movies)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}