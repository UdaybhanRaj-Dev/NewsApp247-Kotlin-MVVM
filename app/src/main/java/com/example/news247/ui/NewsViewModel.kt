package com.example.news247.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.news247.models.Article
import com.example.news247.models.NewsResponse
import com.example.news247.repository.NewsRepository
import com.example.news247.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(app: Application,val newsRepository: NewsRepository) : AndroidViewModel(app)
{

    val headline: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var headlinePage = 1
    var headlineResponse: NewsResponse? = null

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewspage = 1
    var searchNewResponce: NewsResponse? = null
    var newSearchQuery: String? = null
    var oldSearchQuery: String? = null
    init {
        getHeadlines("us")
    }

    fun getHeadlines(countryCode: String)=viewModelScope.launch {
        headlineInternate(countryCode)
    }
    fun searchNews(searchQuery: String)=viewModelScope.launch {
        searchNewsInternate(searchQuery)
    }

    private fun handdleHeadlinesReponse(response:  Response<NewsResponse>): Resource<NewsResponse> {

        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                headlinePage++
                if (headlineResponse == null) {
                    headlineResponse=resultResponse
                }
                else
                {
                    searchNewspage++
                    val oldArtival=headlineResponse?.articles
                    val newArtical = resultResponse.articles
                    oldArtival?.addAll(newArtical)
                }

                return Resource.Success( headlineResponse?:resultResponse)
            }

        }
        return Resource. Error(response.message())

    }
    private fun handleSearchNewsReponse(reponse: Response<NewsResponse>): Resource<NewsResponse>
    {

        if (reponse.isSuccessful)
        {
            reponse.body()?.let { resultRespomnse ->
                if (searchNewResponce==null || newSearchQuery!=oldSearchQuery )
                {
                    searchNewspage=1
                    oldSearchQuery=newSearchQuery
                    searchNewResponce=resultRespomnse
                }
                else {
                    searchNewspage++
                    val oldArticles=searchNewResponce?.articles
                    val newArticles=resultRespomnse.articles
                    oldArticles?.addAll(newArticles)

                }
                return Resource.Success(searchNewResponce?:resultRespomnse)
            }
        }
        return Resource.Error(reponse.message())
    }

    fun addToFavourites(article: Article)=viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun getfavouritesitem()=newsRepository.getFavourititemNews()

    fun deleteArticles(article: Article)=viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

    fun internetConnection(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        return capabilities?.run {
            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        } ?: false
    }

    private suspend fun headlineInternate(countryCode:String)
    {
        headline.postValue(Resource.Loading())
        try {
            if(internetConnection(this.getApplication())) {
                val response = newsRepository.getHeadlines(countryCode,headlinePage)
                headline.postValue(handdleHeadlinesReponse(response))
            }
            else

                {
                    headline.postValue(Resource.Error("No Internate Connection "))

                }
        }
        catch (t: Throwable)
        {
            when(t){
                is IOException->headline.postValue(Resource.Error("Unable to Internate "))
                else -> headline.postValue(Resource.Error("No Signle "))
            }

        }

    }

    private suspend fun searchNewsInternate(searchQuery:String ){
        newSearchQuery=searchQuery
        searchNews.postValue(Resource.Loading())
        try {
            if (internetConnection(this.getApplication()))
            {
                val response=newsRepository.searchNews(searchQuery , searchNewspage)
                searchNews.postValue(handleSearchNewsReponse(response))
            } else {
                searchNews.postValue(Resource.Error("No internate connection"))
            }

        }
        catch (t: Throwable){
            when(t)
            {
                is IOException -> searchNews.postValue(Resource.Error("Unable to connection"))
                else -> searchNews.postValue(Resource.Error("No Signle "))

            }



        }
    }


}