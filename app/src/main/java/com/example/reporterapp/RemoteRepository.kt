package com.example.reporterapp

import android.util.Log
import android.widget.Toast
import com.apollographql.apollo.ApolloCall
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.exception.ApolloHttpException
import com.example.reporterapp.api.ApolloClient
import com.example.reporterapp.app.PreferenceManager
import com.example.reporterapp.database.table.Article
import com.example.reporterapp.database.table.Category
import com.example.reporterapp.database.table.Language
import com.example.reporterapp.database.table.SubCategory
import com.example.reporterapp.type.ArticleInput
import com.example.reporterapp.type.UpdateArticleInput
import com.example.reporterapp.viewmodel.CreateArticleViewModel
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext


/**
 * This class used to communicate data from server.
 * Currently we're using apolloClient for server connection but can be changed to other service like RetroFit or volley
 * We have to change second parameter as Repository
 *
 * */
class RemoteRepository(val model: CreateArticleViewModel) {

    // lateinit var context: Context

    var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    val scope = CoroutineScope(coroutineContext)

    val apolloClient by lazy { ApolloClient.setupApollo() }
    //val service = RetrofitFactory.makeRetrofitService()

    //val client by lazy { setupApollo() }
    init {

    }

    fun getAllCategories() {
        apolloClient.query(GetArticleCategoriesListQuery.builder().build()).enqueue(object :
            ApolloCall.Callback<GetArticleCategoriesListQuery.Data>() {
            override fun onFailure(e: ApolloException) {
                print(e)
            }

            override fun onResponse(response: Response<GetArticleCategoriesListQuery.Data>) {
                val allCategories = response.data()?.getCategories()
                val totalCategories = allCategories?.size ?: 0
                //model.deleteAllCategory()
                if (totalCategories != 0) {
                    repeat(allCategories?.size ?: 0) {
                        var id = allCategories?.get(it)?.id()
                        if (id == null) id = "0"

                        model.insert(Category(id.toInt(), allCategories?.get(it)?.categoryname() ?: ""))
                        val sub = allCategories?.get(it)?.subcategorySet()
                        val subSize = sub?.size ?: 0
                        repeat(subSize) {
                            var subId = sub?.get(it)?.id()
                            if (subId == null) subId = "0"
                            val subCat = SubCategory(subId.toInt(), sub?.get(it)?.subcategoryname() ?: "", id.toInt())
                            Log.e("Remote", subCat.toString())
                            model.insert(subCat)
                        }
                    }
                }
            }

        })
    }

    fun getLanguagesFromApi() {
        apolloClient.query(GetLanguagesQuery.builder().build()).enqueue(object :
            ApolloCall.Callback<GetLanguagesQuery.Data>() {
            override fun onFailure(e: ApolloException) {
                println("Error")
            }

            override fun onResponse(response: Response<GetLanguagesQuery.Data>) {
                val getAllLanguages = response.data()?.getLanguages()
                val languageSize = getAllLanguages?.size ?: 0
                //model.deleteAllLanguage()
                if (languageSize != 0) {
                    repeat(languageSize) {
                        var id = getAllLanguages?.get(it)?.id()
                        if (id == null) id = "0"
                        model.insert(
                            Language(
                                id.toInt(),
                                getAllLanguages?.get(it)?.languagename() ?: "",
                                getAllLanguages?.get(it)?.languagedescription() ?: "",
                                getAllLanguages?.get(it)?.languagelocality() ?: ""
                            )
                        )
                    }

                }
            }
        })
    }

    /*fun getAllArticlesList(localTime: String) {
        try {
            apolloClient.query(
                UserPostedArticlesByTokenQuery.builder().token(
                    PreferenceManager.getInstance(model.getApplication()).token ?: ""
                ).build()
            ).enqueue(object : ApolloCall.Callback<UserPostedArticlesByTokenQuery.Data>() {
                override fun onFailure(e: ApolloException) {
                    return
                    // Toast.makeText(context,"Request Failed",Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(response: Response<UserPostedArticlesByTokenQuery.Data>) {
                    val articleList = response.data()?.UserPostedArticleByStatus()
                    val totalArticles = articleList?.size ?: 0


                    if (totalArticles != 0) {
                        repeat(articleList?.size ?: 0) {
                            //  var listId = articleList?.get(it)?
                            //  if (listId == null)
                            //     listId = "0"
                            Log.e("RemoteRepository", it.toString())
                            model.getArticleCount(articleList?.get(it)?.id()?:"")
                            if(model.count <=0 ){
                               model.insert(
                                   Article(
                                       articleList?.get(it)?.articletitle() ?: "",
                                       articleList?.get(it)?.articledescription() ?: "",
                                       articleList?.get(it)?.language()?.languagename() ?: "",
                                       articleList?.get(it)?.category()?.categoryname() ?: "",
                                       articleList?.get(it)?.subcategory()?.subcategoryname() ?: "",
                                       "Hyderabad",
                                       articleList?.get(it)?.articlestatusSet()?.get(0)?.statusname()?.name?.toUpperCase()
                                           ?: "",
                                       Calendar.getInstance().timeInMillis,
                                       getKeywords(articleList?.get(it)?.articlekeywordsSet()),
                                       listOf(articleList?.get(it)?.attachmentsSet()?.get(0)?.filename()?:""), listOf(),articleList?.get(it)?.id()?:""
                                   )
                               )
                            }
                            else {
                                model.update(
                                    articleList?.get(it)?.id() ?: "",
                                    articleList?.get(it)?.language()?.languagename() ?: "",
                                    articleList?.get(it)?.category()?.categoryname() ?: "",
                                    articleList?.get(it)?.subcategory()?.subcategoryname() ?: "",
                                    "Hyderabad",
                                    articleList?.get(it)?.articletitle() ?: "",
                                    articleList?.get(it)?.articledescription() ?: "",
                                    getKeywords(articleList?.get(it)?.articlekeywordsSet()),
                                    listOf(articleList?.get(it)?.attachmentsSet()?.get(0)?.filename()?:""),
                                    listOf(),
                                    articleList?.get(it)?.articlestatusSet()?.get(0)?.statusname()?.name?.toUpperCase()
                                        ?: "",
                                    date!!
                                )
                            }
                        }
                    }

                }

            })

        } catch (e: ApolloHttpException) {
            e.printStackTrace()
        }

    }*/

    fun getAllArticlesListWithTimeStamp(localTime: String?) {
        try {
            apolloClient.query(
                GetDashboardArticlesByTokenQuery.builder().datetimestamp(localTime).token(
                    PreferenceManager.getInstance(model.getApplication()).token ?: ""
                ).build()
            ).enqueue(object : ApolloCall.Callback<GetDashboardArticlesByTokenQuery.Data>() {
                override fun onFailure(e: ApolloException) {
                    return
                    // Toast.makeText(context,"Request Failed",Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(response: Response<GetDashboardArticlesByTokenQuery.Data>) {
                    val articleList = response.data()?.getDashboardArticles()
                    val totalArticles = articleList?.size ?: 0

                    //model.deleteAllArticle()

                    if (totalArticles != 0) {
                        repeat(articleList?.size ?: 0) {
                            //  var listId = articleList?.get(it)?
                            //  if (listId == null)
                            //     listId = "0"
                            var date: Long? = System.currentTimeMillis()
                            var modifiedTime: Long? = System.currentTimeMillis()
                            if(articleList?.get(it)?.article?.createdDate()?.isNotEmpty()!!) {
                                val modifiedDate = articleList?.get(it)?.article?.createdDate()
                                date = modifiedDate?.let { it1 -> dateFormatter(it1) }
                            }
                            if(articleList?.get(it)?.article?.modifiedAt() != null && articleList?.get(it)?.article?.modifiedAt()?.toString()?.isNotEmpty()!!) {
                                modifiedTime = convertToNewFormat(articleList?.get(it)?.article?.modifiedAt().toString())
                            }
                            var commenDate: String = ""
                            var comment: String = ""
                            if(articleList?.get(it)?.article?.commentsSet!= null && articleList?.get(it)?.article?.commentsSet?.size?:0 > 0 ) {
                                 val commentList = articleList?.get(it)?.article?.commentsSet
                                if (commentList != null) {
                                    for (item in commentList) {
                                        if (item.commentsTo.toString().isNotEmpty() && item.commentsTo.toString().equals("Reporter",ignoreCase = true)) {
                                            val commentDate = convertToNewFormat(item.modifiedAt.toString())
                                            commenDate = getLastLoginString(commentDate)
                                            comment = item.comment
                                            break
                                        }
                                    }
                                }
                            }
                            Log.e("RemoteRepository", it.toString())
                            model.getArticleCount(articleList?.get(it)?.article?.id()?:"")
                            if(model.count <=0 ){
                                model.insert(
                                    Article(
                                        articleList?.get(it)?.articletitle() ?: "",
                                        articleList?.get(it)?.articledescription() ?: "",
                                        articleList?.get(it)?.language()?.languagename() ?: "",
                                        articleList?.get(it)?.category()?.categoryname() ?: "",
                                        articleList?.get(it)?.subcategory()?.subcategoryname() ?: "",
                                        articleList?.get(it)?.location() ?: "",
                                        articleList?.get(it)?.article?.articlestatusSet()?.get(0)?.statusname()?.name?.toUpperCase()
                                            ?: "",
                                        date!!,
                                        getArticleKeywords(articleList?.get(it)?.articlekeywordsSet()),
                                            getS3AttachmentNames(articleList?.get(it)?.attachmentsSet() as kotlin.collections.MutableList<GetDashboardArticlesByTokenQuery.AttachmentsSet>),
                                        listOf(),articleList?.get(it).featuredImage(),
                                        articleList?.get(it)?.article?.id()?:"",
                                        comment,
                                        commenDate,
                                        articleList?.get(it)?.article?.articlestatusSet()?.get(0)?.globalStatus()?.name?.toUpperCase()
                                            ?: "",
                                        modifiedTime!!

                                    )
                                )
                            }
                            else {
                                model.update(
                                    articleList?.get(it)?.article?.id() ?: "",
                                    articleList?.get(it)?.language()?.languagename() ?: "",
                                    articleList?.get(it)?.category()?.categoryname() ?: "",
                                    articleList?.get(it)?.subcategory()?.subcategoryname() ?: "",
                                    articleList?.get(it)?.location() ?: "",
                                    articleList?.get(it)?.articletitle() ?: "",
                                    articleList?.get(it)?.articledescription() ?: "",
                                    getArticleKeywords(articleList?.get(it)?.articlekeywordsSet()),
                                    getS3AttachmentNames(articleList?.get(it)?.attachmentsSet() as MutableList<GetDashboardArticlesByTokenQuery.AttachmentsSet>),
                                    listOf(),
                                    articleList?.get(it)?.article?.articlestatusSet()?.get(0)?.statusname()?.name?.toUpperCase()
                                        ?: "",articleList?.get(it)?.featuredImage()?:"",
                                    date!!,
                                    comment,
                                    commenDate,
                                    articleList?.get(it)?.article?.articlestatusSet()?.get(0)?.globalStatus()?.name?.toUpperCase()
                                        ?: "",
                                    modifiedTime!!
                                )
                            }
                        }
                    }

                }

            })

        } catch (e: ApolloHttpException) {
            e.printStackTrace()
        }

    }


    /*fun getKeywords(keywords: List<UserPostedArticlesByTokenQuery.ArticlekeywordsSet>?): String {

        if (keywords == null) return ""

        var string = ""
        for (x in keywords) {
            string += x.keywords().keyword()
        }
        return string
    }*/

    fun getArticleKeywords(keywords: List<GetDashboardArticlesByTokenQuery.ArticlekeywordsSet>?): String {

        if (keywords == null) return ""

        var string = ""
        for (x in keywords) {
            string += x.keywords().keyword()+","
        }
        var keywordsString = ""
        if(string.isNotEmpty())
            keywordsString = string.substring(0, string.length -1)
        return keywordsString
    }

    fun getS3AttachmentNames(attachmentsSet: MutableList<GetDashboardArticlesByTokenQuery.AttachmentsSet>): List<String> {

        if (attachmentsSet == null) return listOf()

        var s3AttachedFilesList = mutableSetOf<String>()
        for (x in attachmentsSet) {
            s3AttachedFilesList.add(x.filename)
        }
        return s3AttachedFilesList.toMutableList()
    }

    fun postToServer(article: Article) = scope.launch(Dispatchers.IO) {
            val deferred = apolloClient.mutate(
                CreateArticleMutation.builder().articleData(
                    ArticleInput.builder().body(article.articleDes).title(article.articleTitle).category(article.catID).subcategory(
                        article.subCatID
                    ).language(article.languageName).location(article.location).keywords(article.keywords).status(
                        article.status
                    ).featuredimage(
                        ""
                    ).files(article.s3Files).build()
                ).token(com.example.reporterapp.app.PreferenceManager.getInstance(model.getApplication()).token ?: "").build()
            )
                .toDeferred()
            val response = deferred.await()
            var modifiedTime: Long? = System.currentTimeMillis()
            if(!response.hasErrors()){
                if(response.data()?.createArticle()?.article()?.article?.modifiedAt() != null && response.data()?.createArticle()?.article()?.article?.modifiedAt()?.toString()?.isNotEmpty()!!) {
                    modifiedTime = convertToNewFormat(response.data()?.createArticle()?.article()?.article?.modifiedAt().toString())
                }
                article.id = response.data()?.createArticle()?.article()?.article?.id() ?: ""
                article.modifiedTime = modifiedTime!!
                model.insert(article)
            }
            Log.e("article", response.data().toString())

    }

    fun updateArticleToServer(article: Article, isInsert: Boolean) = scope.launch(Dispatchers.IO) {

        val deferred = apolloClient.mutate(
            UpdateArticleMutation.builder().article(
                UpdateArticleInput.builder().articleId(article.id).body(article.articleDes).title(article.articleTitle).category(article.catID).subcategory(
                    article.subCatID
                ).language(article.languageName).location(article.location).keywords(article.keywords).status(
                    article.status
                ).featuredimage(
                    article.featuredImage
                ).files(article.s3Files).comment(article.comment).build()
            ).token(com.example.reporterapp.app.PreferenceManager.getInstance(model.getApplication()).token ?: "").build()
        )
            .toDeferred()
        val response = deferred.await()
        var modifiedTime: Long? = System.currentTimeMillis()
        if(isInsert) {
            if(response.data()?.updateArticleById()?.article()?.article?.modifiedAt() != null && response.data()?.updateArticleById()?.article()?.article?.modifiedAt()?.toString()?.isNotEmpty()!!) {
                modifiedTime = convertToNewFormat(response.data()?.updateArticleById()?.article()?.article?.modifiedAt().toString())
            }
            article.modifiedTime = modifiedTime!!
        }
        model.insert(article)
        if(!response.hasErrors()){

        }
        Log.e("article", response.data().toString())

    }

    fun deleteArticleFromServer(articleId: Int) = scope.launch(Dispatchers.IO) {

        val deferred = apolloClient.mutate(
            DeleteArticleMutation.builder().
                articleId(articleId).token(com.example.reporterapp.app.PreferenceManager.getInstance(model.getApplication()).token ?: "")
                .build()
            )

            .toDeferred()
        val response = deferred.await()
        if(!response.hasErrors()){
            model.delete(articleId)
        }
        Log.e("article", response.data().toString())

    }
}
