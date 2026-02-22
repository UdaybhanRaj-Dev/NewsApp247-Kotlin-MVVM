package com.example.news247.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.news247.Constants
import com.example.news247.Constants.Companion.SEARCH_NEWS_TIME_DELAY
import com.example.news247.R
import com.example.news247.adapters.NewsAdapter
import com.example.news247.databinding.FragmentSearchBinding
import com.example.news247.ui.NewsActivity
import com.example.news247.ui.NewsViewModel
import com.example.news247.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchFragment : Fragment(R.layout.fragment_search) {

    lateinit var newsViewModel: NewsViewModel
    lateinit var newsAdapter: NewsAdapter
    lateinit var retryButton: Button
    lateinit var errorText : TextView
    lateinit var itemSearchError: CardView
    lateinit var binding: FragmentSearchBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding= FragmentSearchBinding.bind(view)

        itemSearchError=view.findViewById(R.id.itemSearchError)
        val inflater=requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View=inflater.inflate(R.layout.item_error,null)
        retryButton=view.findViewById(R.id.rtybtn)
        errorText=view.findViewById(R.id.errortext)
        newsViewModel=(activity as NewsActivity).newsViewModel
        setupSearchRecycler()
        newsAdapter.setOnItemClickListner {
            val bundle= Bundle().apply {
                putSerializable("article",it)
            }
            findNavController().navigate(R.id.action_searchFragment_to_articalFragment,bundle)

        }
        var job: Job?=null
        binding.searchbar.addTextChangedListener(){editable ->
            job?.cancel()
            job= MainScope().launch {
                delay(SEARCH_NEWS_TIME_DELAY)
                editable?.let {
//                    if (editable.toString().isNotEmpty())
//                    {
//                        newsViewModel.searchNews(editable.toString())
//                    }
                    val query = editable.toString().trim()

                    if (query.length >= 3) {
                        newsViewModel.searchNews(query)
                    } else {
                        hideErrorMessage()
                    }

                }
            }


        }

        newsViewModel.searchNews.observe(viewLifecycleOwner, Observer { response ->


            when(response)
            {
                is Resource.Success<*> -> {

                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPage=newsResponse.totalResults / Constants.QUERY_PAGESIZE + 2
                        isLastPage=newsViewModel.searchNewspage==totalPage
                        if(isLastPage)
                        {
                            binding.searchRview.setPadding(0,0,0,0)
                        }


                    }
                }
//                is Resource.Error<*> -> {
//
//                    hideProgressBar()
//                    response.message?.let{ message ->
//                        Toast.makeText(activity,"Sorry error :  $message" , Toast.LENGTH_SHORT).show()
//                        showErrorMessage(message)
//
//
//
//                    }
//
//                }

                is Resource.Error -> {
                    hideProgressBar()

                    response.message?.let { message ->

                        if (message.contains("Unable to resolve host") ||
                            message.contains("timeout") ||
                            message.contains("Failed to connect")
                        ) {
                            showErrorMessage("No Internet Connection")
                        }

                        else {
                            hideErrorMessage()  // normal API error ignore
                        }
                    }
                }



                is Resource.Loading<*> -> {
                    showProgressBar()
                }
            }

        })

        retryButton.setOnClickListener {

            if (binding.searchbar.text.toString().isNotEmpty())
            {

                newsViewModel.searchNews(binding.searchbar.text.toString())
            }
            else
            {
                hideErrorMessage()
            }
        }



    }



    var isError=false
    var isLoading=false
    var isLastPage=false
    var isScrolling=false

    private fun hideProgressBar(){
        binding.progressbar.visibility=View.INVISIBLE
        isLoading=false
    }

    private fun showProgressBar()
    {
        binding.progressbar.visibility=View.VISIBLE
        isLoading=true


    }

    private fun hideErrorMessage()
    {
        itemSearchError.visibility=View.INVISIBLE
        isError=false

    }


    private fun  showErrorMessage(message: String)
    {
        itemSearchError.visibility=View.VISIBLE
        errorText.text=message
        isError=true

    }

    val scrollListner=object : RecyclerView.OnScrollListener()
    {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager=recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition=layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount=layoutManager.childCount
            val totalItemCount=layoutManager.itemCount


            val isNoError= !isError
            val isNotLoaadingAndNotLastPage=!isLoading && !isLastPage
            val isAtLastItem=firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning=firstVisibleItemPosition>=0
            val isTotalMoreThanVisible=totalItemCount>= Constants.QUERY_PAGESIZE
            val shouldPaginate=isNoError && isNotLoaadingAndNotLastPage &&  isAtLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling
            if(shouldPaginate)
            {
                newsViewModel.searchNews(binding.searchbar.text.toString())
                isScrolling=false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if(newState== AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
            {
                isScrolling=true
            }
        }
    }


    private fun setupSearchRecycler()
    {
        newsAdapter= NewsAdapter()
        binding.searchRview.apply{
            adapter=newsAdapter
            layoutManager= LinearLayoutManager(activity)
            addOnScrollListener(this@SearchFragment.scrollListner)
        }
    }



}