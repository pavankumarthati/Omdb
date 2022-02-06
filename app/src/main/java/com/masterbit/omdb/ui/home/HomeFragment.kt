package com.masterbit.omdb.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.masterbit.omdb.OMDBApp
import com.masterbit.omdb.R
import com.masterbit.omdb.databinding.FragmentHomeBinding
import com.masterbit.omdb.ui.moviedetail.MOVIE_ID_KEY
import com.masterbit.omdb.ui.moviedetail.MovieDetailActivity

class HomeFragment : Fragment() {

    private val homeViewModel by viewModels<HomeViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel((requireActivity().application as OMDBApp).movieDatabase) as T
            }
        }
    }
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.movieList.adapter = MovieListAdapter({
            // start Movie Detail Activity
            startActivity(Intent(this.requireContext(), MovieDetailActivity::class.java).apply {
                putExtras(bundleOf(MOVIE_ID_KEY to it))
            })

        }, { movieId, isChecked ->
            homeViewModel.updateFavorite(movieId, isChecked)
        })
        homeViewModel.movieResultsLiveData.observe(this.viewLifecycleOwner) {
            (binding.movieList.adapter as MovieListAdapter).submitList(it)
        }

        homeViewModel.progressLiveData.observe(this.viewLifecycleOwner) {
            binding.progressBar.isVisible = it
            binding.movieList.isVisible = !it
        }

        binding.searchBtn.setOnClickListener {
            homeViewModel.search(binding.titleEt.text.toString(), binding.yearEt.text.toString(), binding.idEt.text.toString())
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}