package com.example.news247.ui.fragments


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.navigation.fragment.navArgs
import com.example.news247.R
import com.example.news247.databinding.FragmentArticalBinding
import com.example.news247.ui.NewsActivity
import com.example.news247.ui.NewsViewModel
import com.google.android.material.snackbar.Snackbar

//
class ArticalFragment : Fragment(R.layout.fragment_artical) {
    lateinit var newsViewModel: NewsViewModel
    val args: ArticalFragmentArgs by navArgs()
    lateinit var binding: FragmentArticalBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentArticalBinding.bind(view)

        newsViewModel = (activity as NewsActivity).newsViewModel
        val article = args.article

        binding.webview.apply {
            webViewClient = WebViewClient()
            article.url?.let {
                loadUrl(it)
            }
        }
         binding.fab.setOnClickListener {
             newsViewModel.addToFavourites(article)
             Snackbar.make(view,"Add to Favourtites ", Snackbar.LENGTH_SHORT).show()
         }

    }
}