package com.example.news247.ui.fragments


import android.content.ClipData
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.example.news247.R
import com.example.news247.adapters.NewsAdapter
import com.example.news247.databinding.FragmentArticalBinding
import com.example.news247.databinding.FragmentFavouritesBinding
import com.example.news247.ui.NewsActivity
import com.example.news247.ui.NewsViewModel
import com.google.android.material.snackbar.Snackbar


class FavouritesFragment : Fragment(R.layout.fragment_favourites) {


    lateinit var newsViewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    lateinit var binding: FragmentFavouritesBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFavouritesBinding.bind(view)

        newsViewModel = (activity as NewsActivity).newsViewModel
        setupFavouritesRecycler()
        newsAdapter.setOnItemClickListner {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_favouritesFragment_to_articalFragment, bundle)
        }
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        )
        {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                val position=viewHolder.adapterPosition
                val article=newsAdapter.differ.currentList[position]
                newsViewModel.deleteArticles(article)
                Snackbar.make(view,"Removed from favourtise ", Snackbar.LENGTH_LONG).apply {
                    setAction("Undo"){
                        newsViewModel.addToFavourites(article)
                    }
                    show()
                }

            }

        }

        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.RecyclerViewfavourtites)
        }
        newsViewModel.getfavouritesitem().observe(viewLifecycleOwner , Observer {articles ->

            newsAdapter.differ.submitList(articles)
        })


    }


    private fun setupFavouritesRecycler()
    {

        newsAdapter= NewsAdapter()
        binding.RecyclerViewfavourtites.apply{
            adapter=newsAdapter
            layoutManager= LinearLayoutManager(activity)

        }
    }
}