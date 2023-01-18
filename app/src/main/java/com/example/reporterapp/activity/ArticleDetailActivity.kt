package com.example.reporterapp.activity

import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import com.example.reporterapp.adapter.KeyAdapter
import com.example.reporterapp.viewmodel.ArticleViewModel
import android.support.v7.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.reporterapp.*
import com.example.reporterapp.app.PreferenceManager
import com.example.reporterapp.database.table.Article
import com.example.reporterapp.viewmodel.CreateArticleViewModel
import kotlinx.android.synthetic.main.activity_article_detail.*
import java.util.concurrent.TimeUnit


/**  */
class ArticleDetailActivity : AppCompatActivity() {
    private lateinit var articleViewModel: ArticleViewModel
    private lateinit var createArticleViewModel: CreateArticleViewModel
    private var articleId = -1
    private var articleLocalId = -1
    private var articleStatus = ArticleStatus.NONE
    private var article:Article? = null
    lateinit var loader: Dialog
    var modifiedTime: Long? = System.currentTimeMillis()
    var createdTime: Long? = System.currentTimeMillis()

    private var dialogClickListener: DialogInterface.OnClickListener =
        DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    createArticleViewModel.deleteArticle(articleId)
                    //articleViewModel.delete(articleId)
                    finish()
                }

                DialogInterface.BUTTON_NEGATIVE -> {

                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_detail)

        articleId = intent.getIntExtra(ARTICLE_ID, -1)
        articleLocalId = intent.getIntExtra("artile_local_id", -1)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.article_details)

        loader = getAlertDialog(this)
        loader.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

 /*       if (articleId == -1) {
            RuntimeException(resources.getString(R.string.set_article_id_if_you_need_article_details))
            return
        }*/

        val keywordAdapter = KeyAdapter()
        val attachmentAdapter = KeyAdapter()

    /*    val staggeredGridLayoutManager = StaggeredGridLayoutManager(4, LinearLayoutManager.VERTICAL)
        rvGetArticleKeywords.layoutManager = staggeredGridLayoutManager;
        rvGetArticleKeywords.adapter = keywordAdapter*/

        rvGetArticleKeywords.layoutManager = LinearLayoutManager(this, LinearLayout.HORIZONTAL, false)
        rvGetArticleKeywords.adapter = keywordAdapter

        rvGetAttachment.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        rvGetAttachment.adapter = attachmentAdapter

        createArticleViewModel = ViewModelProviders.of(this).get(CreateArticleViewModel::class.java)
        articleViewModel = ViewModelProviders.of(this).get(ArticleViewModel::class.java)
        articleViewModel.setData(articleLocalId)

        articleViewModel.searchByLiveData?.observe(this, Observer { article ->
            article?.let {
                this.article = article
                modifiedTime = article.modifiedTime
                createdTime = article.time
                val createdDate = getDateString(createdTime!!)
                val currentTime = getDateString(System.currentTimeMillis())
                val modifiedDate = getDateString(modifiedTime!!)
                val diff = dateAndYearFormatter(currentTime) - dateAndYearFormatter(modifiedDate)
                System.out.println("Days: " + TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS))
                if(article.status.equals("Pending", ignoreCase = true)) {
                    val noOfDays = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
                    days.visibility = View.VISIBLE
                    if(noOfDays == 1L )
                        days.text = "Submitted "+TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + " Day ago "
                    else if(noOfDays > 0 )
                        days.text = "Submitted "+TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + " Days ago "
                    else if (noOfDays == 0L)
                        days.text = "Submitted Today"
                    else if (noOfDays > 30)
                        days.text = "Submitted 1 Month ago"
                    else if (noOfDays > 60)
                        days.text = "Submitted 2 Months ago"
                    else if (noOfDays > 90)
                        days.text = "Submitted 3 Months ago"
                    else if (noOfDays > 120)
                        days.text = "Submitted 4 Months ago"
                    else if (noOfDays > 150)
                        days.text = "Submitted 5 Months ago"
                    else if (noOfDays > 180)
                        days.text = "Submitted 6 Months ago"
                    else if (noOfDays > 210)
                        days.text = "Submitted 7 Months ago"
                    else if (noOfDays > 240)
                        days.text = "Submitted 8 Months ago"
                    else if (noOfDays > 270)
                        days.text = "Submitted 9 Months ago"
                    else if (noOfDays > 300)
                        days.text = "Submitted 10 Months ago"
                    else if (noOfDays > 330)
                        days.text = "Submitted 11 Months ago"
                    else if (noOfDays > 360)
                        days.text = "Submitted 1 Year ago"
                    else
                        days.visibility = View.GONE
                }
                else{
                    days.visibility = View.GONE
                }
                tvViewArticleTitle.text = article.articleTitle
                tvViewArticleStatusTag.text = article.status
                tvGetArticleTitle.text = article.articleTitle
                val desc = article.articleDes.replace("\n", "<br>");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tvGetArticleDescription.text = Html.fromHtml(desc,Html.FROM_HTML_MODE_COMPACT);
                } else {
                    tvGetArticleDescription.text = Html.fromHtml(desc);
                }

                //tvGetArticleDescription.text = article.articleDes
                tvGetLocation.text = article.location


                keywordAdapter.setData(article.keywords.split(",").toList(), false, this)
                attachmentAdapter.setData(article.files.map { getFileName(it) }, true, this)

                if (article.files.isEmpty())
                    tvTextattachment.visibility = View.GONE

                tvGetLanguage.text = article.languageName
                tvGetCategory.text = article.catID
                tvGetSubCategory.text = article.subCatID

                articleStatus = ArticleStatus.values().find {
                    it.caption== if(article.status.equals("Rework", ignoreCase = true)) "RE-WORK"
                    else if (article.status.equals("Draft", ignoreCase = true) &&
                        article.globalStatus.equals("Rework", ignoreCase = true)) "RE-WORK"
                    else if (article.status.equals("Draft", ignoreCase = true)) "DRAFTS" else
                    article.status }?:ArticleStatus.NONE


                    if (articleStatus == ArticleStatus.REJECTED || articleStatus == ArticleStatus.RE_WORK) {
                        findViewById<CardView>(R.id.cvComments).visibility = View.VISIBLE
                        if(article.comment != null)
                        findViewById<TextView>(R.id.tvArticleRejectedReason).text = article.comment
                        if(article.commentDate != null)
                        findViewById<TextView>(R.id.tvArticleRejectedDay).text = article.commentDate
                    }
                    else findViewById<CardView>(R.id.cvComments).visibility =View.GONE
                invalidateOptionsMenu()
            }
        })


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_article_details, menu)

        if(articleStatus.ordinal == ArticleStatus.DRAFT.ordinal) {
            menu?.findItem(R.id.action_edit)?.isVisible = true
            menu?.findItem(R.id.action_deleted)?.isVisible = true
            menu?.findItem(R.id.action_send)?.isVisible = true
        }
        else if(articleStatus.ordinal == ArticleStatus.RE_WORK.ordinal){
            menu?.findItem(R.id.action_edit)?.isVisible = true
            menu?.findItem(R.id.action_deleted)?.isVisible = false
            menu?.findItem(R.id.action_send)?.isVisible = true
        }
        else{
            menu?.findItem(R.id.action_edit)?.isVisible = false
            menu?.findItem(R.id.action_deleted)?.isVisible = false
            menu?.findItem(R.id.action_send)?.isVisible = false
        }


        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_edit ->
                startActivity(Intent(this, CreateArticleActivity::class.java).apply {
                    putExtra(IS_EDIT, true)
                    putExtra(ARTICLE_ID, articleId)
                    putExtra("artile_local_id", articleLocalId)
                    putExtra("createdTime", createdTime)
                    putExtra("modifiedTime", modifiedTime)
                })
            R.id.action_send ->
                if(article!=null) {
                    if(articleStatus == ArticleStatus.RE_WORK)
                        commentsDialog().show()
                    else
                        directSubmit()
                }
            R.id.action_deleted -> {
                showDeleteDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDeleteDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.want_to_delete_this_article).setPositiveButton(R.string.yes, dialogClickListener)
            .setNegativeButton(R.string.no, dialogClickListener).show()
    }

   /* private fun showsubmitDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do you want to submit this article?").setPositiveButton(R.string.yes, submitDialogClickListener)
            .setNegativeButton(R.string.no, submitDialogClickListener).show()
    }

    private var submitDialogClickListener: DialogInterface.OnClickListener =
        DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    loader.show()
                    article!!.status = ArticleStatus.values()[ArticleStatus.PENDING.ordinal].caption
                    createArticleViewModel.updateArticle(article!!, true)
                    showMsg(application, "Article Saved Successfully", Toast.LENGTH_SHORT)
                    loader.dismiss()
                    finishAffinity()
                    intent = Intent(this, MainActivity::class.java)
                    intent.putExtra(
                        "localTime",
                        if (PreferenceManager.getInstance(applicationContext).refresh_date == null) localToUTCForArticles() else
                            PreferenceManager.getInstance(applicationContext).refresh_date
                    )
                    intent.putExtra("isLoad", false)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }

                DialogInterface.BUTTON_NEGATIVE -> {

                }
            }
        }*/

    private fun commentsDialog():android.support.v7.app.AlertDialog.Builder {
        val builder = android.support.v7.app.AlertDialog.Builder(this)
        val li = LayoutInflater.from(this);
        val v = li.inflate(R.layout.dialog_comments, null)
        builder.setView(v);
        val userInput = v.findViewById<EditText>(R.id.input);
        builder.setPositiveButton(R.string.submit) { dialog, which ->
            dialog.cancel()
            article!!.comment = userInput.text.toString()
            loader.show()
            article!!.status = ArticleStatus.values()[ArticleStatus.PENDING.ordinal].caption
            createArticleViewModel.updateArticle(article!!, true)
            showMsg(application, "Article Saved Successfully", Toast.LENGTH_SHORT)
            loader.dismiss()
            finishAffinity()
            intent = Intent(this, MainActivity::class.java)
            intent.putExtra(
                "localTime",
                if (PreferenceManager.getInstance(applicationContext).refresh_date == null) localToUTCForArticles() else
                    PreferenceManager.getInstance(applicationContext).refresh_date
            )
            intent.putExtra("isLoad", false)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            /*  if(userInput.text.toString().trim().isNullOrEmpty()){
                  showMsg(this, "Please provide comments!",Toast.LENGTH_SHORT)
                  return@setPositiveButton
              }else {
                  dialog.dismiss()
                  m_Text = userInput.text.toString()
              }*/
        }
        builder.setNegativeButton(
            R.string.cancel
        ) { dialog, which ->
            loader.dismiss()
            dialog.cancel() }

        return builder
    }
    private fun directSubmit() {
            loader.show()
            article!!.status = ArticleStatus.values()[ArticleStatus.PENDING.ordinal].caption
            createArticleViewModel.updateArticle(article!!, true)
            showMsg(application, "Article Saved Successfully", Toast.LENGTH_SHORT)
            loader.dismiss()
            finishAffinity()
            intent = Intent(this, MainActivity::class.java)
            intent.putExtra(
                "localTime",
                if (PreferenceManager.getInstance(applicationContext).refresh_date == null) localToUTCForArticles() else
                    PreferenceManager.getInstance(applicationContext).refresh_date
            )
            intent.putExtra("isLoad", false)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}
