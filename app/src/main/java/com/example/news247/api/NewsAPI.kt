package com.example.news247.api

import com.example.news247.Constants
import com.example.news247.models.NewsResponse
//import com.example.news247.models.newsReponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsAPI {

    @GET("v2/top-headlines")
    suspend fun getHeadlines(
        @Query("country")
        countryCode: String = "us",
        @Query("page")
        pageNumber: Int = 1,
        @Query("apiKey")
        apiKey: String = Constants.API_KEY
    ): Response<NewsResponse>
    @GET("v2/top-everythings")
    suspend fun NewssearchFor(
        @Query("g")
        searchQuery: String,
        @Query("page")
        pageNumber: Int=1,
        apiKey: String= Constants.API_KEY
    )
    : Response<NewsResponse>


}
