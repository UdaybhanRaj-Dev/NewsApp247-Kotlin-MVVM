package com.example.news247.repository

import com.example.news247.api.RetrofitInstant
import com.example.news247.db.ArticleDatabase
import com.example.news247.models.Article
import java.time.temporal.TemporalQuery
import java.util.Locale

class NewsRepository (val db: ArticleDatabase){
    suspend fun getHeadlines(countryCode: String,pageNumber: Int)=
        RetrofitInstant.api.getHeadlines(countryCode,pageNumber)
    suspend fun searchNews(searchQuery: String,pageNumber: Int)=
        RetrofitInstant.api.NewssearchFor(searchQuery,pageNumber)
    suspend fun upsert(article: Article)=db.getArticleDao().upsert(article)
    fun getFavourititemNews()=db.getArticleDao().getAllArticle()
    suspend fun deleteArticle(article: Article)=db.getArticleDao().deleteArticle(article)

}