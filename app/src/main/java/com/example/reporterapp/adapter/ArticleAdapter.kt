package com.example.reporterapp.adapter

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.example.reporterapp.*
import com.example.reporterapp.activity.ArticleDetailActivity
import com.example.reporterapp.activity.MainActivity
import com.example.reporterapp.app.PreferenceManager
import com.example.reporterapp.callback.RecyclerViewListener
import com.example.reporterapp.database.table.Article
import com.example.reporterapp.fragment.ArticleFragment
import com.example.reporterapp.viewmodel.CreateArticleViewModel
import de.hdodenhof.circleimageview.CircleImageView


class ArticleAdapter(private val recyclerViewListener: RecyclerViewListener) : RecyclerView.Adapter<ArticleAdapter.MyAdapter>(),Filterable {

    var articleList = emptyList<Article>()
    var otherList = emptyList<Article>()
    private lateinit var fragment: ArticleFragment
    private lateinit var createArticleViewModel: CreateArticleViewModel

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MyAdapter {
        val v = LayoutInflater.from(p0.context).inflate(R.layout.article, p0, false)
        createArticleViewModel = ViewModelProviders.of(fragment).get(CreateArticleViewModel::class.java)
        return MyAdapter(v)
    }

    override fun getItemCount(): Int {
        return articleList.size
    }

    var listColor= listOf(R.color.articleColor,R.color.white)
    var categoryColorMap = hashMapOf<String, Int>("Sports" to R.drawable.blue_rounded_corners_filled,
        "Political" to R.drawable.red_rounded_corners_filled,"Television" to R.drawable.vialot_rounded_corners_filled,
        "Movies" to R.drawable.dark_blue_rounded_corners_filled,"Other" to R.drawable.gray_rounded_corners_filled)

    override fun onBindViewHolder(p0: MyAdapter, p1: Int) {

        p0.itemView.findViewById<TextView>(R.id.tvArticleTitle).text = articleList[p1].articleTitle

        Glide.with(this.fragment).load(articleList[p1].featuredImage).centerCrop().placeholder(R.drawable.news_article_icon)
            .into(p0.itemView.findViewById<CircleImageView>(R.id.ivArticleCast))

        p0.itemView.findViewById<TextView>(R.id.tvArticleCreatedDate).text = getDateString(if (articleList[p1].time != null) articleList[p1].time else 0)

        p0.itemView.findViewById<TextView>(R.id.tvArticleTag).text = articleList[p1].catID

        p0.itemView.setOnClickListener {
            recyclerViewListener.recyclerViewListClicked(it, articleList[p0.adapterPosition].id.toInt(),articleList[p0.adapterPosition].localId)
        }

        if(articleList[p1].status .equals("Draft", ignoreCase = true)){
            p0.itemView.findViewById<ImageView>(R.id.delete).visibility = View.GONE
            p0.itemView.findViewById<ImageView>(R.id.send).visibility = View.VISIBLE
        }
        else{
            p0.itemView.findViewById<ImageView>(R.id.delete).visibility = View.GONE
            p0.itemView.findViewById<ImageView>(R.id.send).visibility = View.GONE
        }
        p0.itemView.findViewById<ImageView>(R.id.delete).setOnClickListener {
            val article: Article = articleList[p1]
            createArticleViewModel.delete(article)
        }
        p0.itemView.findViewById<ImageView>(R.id.send).setOnClickListener {
            val article: Article = articleList[p1]
            article.status = ArticleStatus.values()[ArticleStatus.PENDING.ordinal].caption
            //createArticleViewModel.updateArticle(articleList[p1],true)
            if(article.status.equals("Draft",ignoreCase = true) && article.globalStatus.equals("Rework",ignoreCase = true)) {
                commentsDialog(article).show()
            }
            else
                createArticleViewModel.updateArticle(article,true)
            //createArticleViewModel.insert(article)
            notifyItemChanged(articleList[p1].id.toInt())
            fragment.context?.let { it1 -> showMsg(it1,"Submitted Successfully",Toast.LENGTH_SHORT) }
        }
/*
        p0.itemView.setOnClickListener {
            fragment.context?.startActivity(Intent(fragment.context, ArticleDetailActivity::class.java).apply {
                putExtra(ARTICLE_ID,articleList[p1].id)
                putExtra("artile_local_id",articleList[p1].localId)
            })
        }*/

    p0.itemView.findViewById<TextView>(R.id.tvArticleTag).background= categoryColorMap.get(articleList[p1].catID)?.let {
            ContextCompat.getDrawable(p0.itemView.context,
                it
            )
        }

        p0.itemView.findViewById<CardView>(R.id.card).setCardBackgroundColor(ContextCompat.getColor(p0.itemView.context, listColor[p1%2]))

    }

    class MyAdapter(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        override fun onClick(v: View?) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    private fun commentsDialog(article: Article):android.support.v7.app.AlertDialog.Builder {
        val builder = fragment.context?.let { android.support.v7.app.AlertDialog.Builder(it) }
        val li = LayoutInflater.from(fragment.context);
        val v = li.inflate(R.layout.dialog_comments, null)
        builder!!.setView(v);
        val userInput = v.findViewById<EditText>(R.id.input);
        builder.setPositiveButton(R.string.submit) { dialog, which ->
            dialog.cancel()
            article.comment = userInput.text.toString()
            createArticleViewModel.updateArticle(article, true)
        }
        builder.setNegativeButton(
            R.string.cancel
        ) { dialog, which ->
            dialog.cancel() }

        return builder
    }

    fun setData(articleList: List<Article>, fragment: ArticleFragment) {
        this.articleList = articleList
        this.otherList=articleList
        this.fragment = fragment
        notifyDataSetChanged()
    }

    private val filterObject=object : Filter() {
        override fun performFiltering(constraint: CharSequence): FilterResults? {

            val results = FilterResults()
            if (constraint.isEmpty()) {
                results.values = otherList
                results.count = otherList.size
            }else{

                val filteredContacts = ArrayList<Article>()
                val upperCase=constraint.toString().toUpperCase()

                for (article in otherList){

                    var catName=article.catID
                    if(catName==null)catName=""

                    var subCatName= article.subCatID
                    if(subCatName==null)subCatName=""

                    if(article.articleTitle.toUpperCase().contains(upperCase)||
                        article.articleDes.toUpperCase().contains(upperCase) ||
                        article.keywords.toUpperCase().contains(upperCase) ||
                        catName.contains(upperCase)||
                        subCatName.contains(upperCase))
                        filteredContacts.add(article)
                }

                results.values = filteredContacts
                results.count=filteredContacts.size
            }

            return results
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            articleList = results.values as List<Article>
            notifyDataSetChanged()
        }
    }

    override fun getFilter(): Filter {
        return filterObject
    }

    fun setFilter(query:String){
        filter.filter(query)
    }
}