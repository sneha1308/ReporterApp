package com.example.reporterapp.activity

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.SystemClock
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.webkit.MimeTypeMap
import android.widget.*
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.util.IOUtils
import com.example.reporterapp.*
import com.example.reporterapp.adapter.AttachmentAdapter
import com.example.reporterapp.app.PreferenceManager
import com.example.reporterapp.callback.RecyclerViewListener
import com.example.reporterapp.database.table.Article
import com.example.reporterapp.viewmodel.ArticleViewModel
import com.example.reporterapp.viewmodel.CreateArticleViewModel
import com.example.reporterapp.viewmodel.categoryList
import kotlinx.android.synthetic.main.activity_create_article.*
import kotlinx.android.synthetic.main.layout_fab_submenu.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

const val RC_AUDIO_AND_STORAGE = 54
const val RC_CAMERA_AND_LOCATION = 25
const val RECORD_VIDEO_RESULT_CODE = 25
const val RECORD_SOUND_RESULT_CODE = 23
const val PICK_FILE_RESULT_CODE = 45


class CreateArticleActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks, View.OnClickListener {

    private val KEY = "AKIAXDCDWHJZBNJJHHF5"
    private val SECRET = "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"
    lateinit var loader: Dialog

    private var recordingThread: Thread? = null
    private var recordingThread1: Thread? = null
    private var s3Client: AmazonS3Client? = null
    private var credentials: BasicAWSCredentials? = null

    //track Choosing Image Intent
    private val CHOOSING_IMAGE_REQUEST = 1234
    private val RECORDED_AUDIO = 12

    private var fileUri: Uri? = null
    private val bitmap: Bitmap? = null

    private lateinit var articleViewModel: ArticleViewModel
    private lateinit var createArticleViewModel: CreateArticleViewModel
    lateinit var attachmentAdapter: AttachmentAdapter
    var attachedFiles = mutableSetOf<String>()
    lateinit var location: EditText
    lateinit var articleTitle: EditText
    lateinit var articleDes: EditText
    lateinit var language: Spinner

    private lateinit var mediaRecorder: MediaRecorder
    private var isEdit = false
    private var articleId = -1
    private var articleLocalId = -1
    var modifiedTime: Long? = System.currentTimeMillis()
    var createdTime: Long? = System.currentTimeMillis()
    private var isRework = false
    private var globalStatus:String = ""
    private var featuredImage = ""
    private var selectedSubCatPosition = ""
    private var selectedCatPosition = ""
    private var selectedLangPosition = ""

    var s3AttachedFilesList = mutableSetOf<String>()
    val simpleDateFormat = SimpleDateFormat("dd-mm-yyyy-hh-mm-ss")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_article)

        loader = getAlertDialog(this)
        loader.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        /*val progressRunnable = object : Runnable {
            override fun run() {
                if(loader.isShowing) {
                    showMsg(
                        this@CreateArticleActivity,
                        "Timeout! please check your internet connection",
                        Toast.LENGTH_SHORT
                    )
                    loader.dismiss()
                }
            }

        }
        val pdCanceller = Handler();
        pdCanceller.postDelayed(progressRunnable, 100000);*/
        loader.show()
        AWSMobileClient.getInstance().initialize(this).execute()
        credentials = BasicAWSCredentials(KEY, SECRET)
        s3Client = AmazonS3Client(credentials)

        isEdit = intent.getBooleanExtra(IS_EDIT, false)
        articleId = intent.getIntExtra(ARTICLE_ID, -1)
        articleLocalId = intent.getIntExtra("artile_local_id", -1)
        createdTime = intent.getLongExtra("createdTime", System.currentTimeMillis())
        modifiedTime = intent.getLongExtra("modifiedTime", System.currentTimeMillis())

        supportActionBar?.title = resources.getString(if (isEdit) R.string.edit_article else R.string.create_article)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        createArticleViewModel = ViewModelProviders.of(this).get(CreateArticleViewModel::class.java)
        createArticleViewModel.allLanguage
        createArticleViewModel.allCategories
        createArticleViewModel.searchByLiveData
        articleViewModel = ViewModelProviders.of(this).get(ArticleViewModel::class.java)
        createArticleViewModel.getCategories(this)

        val languageSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf(""))
        languageSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tvSelectLanguage?.adapter = languageSpinnerAdapter

        val categorySpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf(""))
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tvSelectCategory?.adapter = categorySpinnerAdapter

        val subCategorySpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mutableListOf(""))
        subCategorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubCategory?.adapter = subCategorySpinnerAdapter

        articleTitle = findViewById(R.id.etArticleTitle)
        location = findViewById(R.id.etArticleLocation)
        location = findViewById(R.id.etArticleDescription)

        val ivAttachNormal = findViewById<ImageView>(R.id.ivAttachNormal)
        val ivVoiceAssistance = findViewById<ImageView>(R.id.ivVoiceAssistance)
        val ivVideo = findViewById<ImageView>(R.id.ivVideo)
        val ivAttach = findViewById<ImageView>(R.id.ivAttach)

        ivVideo.setOnClickListener(this)
        ivAttach.setOnClickListener(this)
        ivVoiceAssistance.setOnClickListener(this)
        ivAttachNormal.setOnClickListener(this)

        mediaRecorder = MediaRecorder()


        createArticleViewModel.allLanguage.observe(this, Observer { languages ->
            languages?.let {
                languageSpinnerAdapter.clear()
                languageSpinnerAdapter.add("Select Language")
                languageSpinnerAdapter.addAll(it.map { n -> n.languageName })
            }
            if(selectedLangPosition.isNotEmpty())
                tvSelectLanguage.setSelection(languageSpinnerAdapter.getPosition(selectedLangPosition))
        })

        createArticleViewModel.allCategories.observe(this, Observer { categories ->
            categories?.let {
                categorySpinnerAdapter.clear()
                categorySpinnerAdapter.add("Select Category")
                categorySpinnerAdapter.addAll(it.map { n -> n.catName })
            }
            if(selectedCatPosition.isNotEmpty())
                tvSelectCategory.setSelection(categorySpinnerAdapter.getPosition(selectedCatPosition))
        })

        tvSelectCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val id = categoryList.find { it.catName == tvSelectCategory.selectedItem.toString() }?.id
                subCategorySpinnerAdapter.clear()
                createArticleViewModel.setData(id ?: 0)
            }
        }

        createArticleViewModel.searchByLiveData?.observe(this, Observer { subCategories ->
            subCategories?.let {
                subCategorySpinnerAdapter.clear()
                subCategorySpinnerAdapter.add("Select Sub-Category")
                subCategorySpinnerAdapter.addAll(it.map { n -> n.subCatName })
            }
            if(selectedSubCatPosition.isNotEmpty())
                spinnerSubCategory.setSelection(subCategorySpinnerAdapter.getPosition(selectedSubCatPosition))
        })

        /*getData(languageSpinnerAdapter, categorySpinnerAdapter, subCategorySpinnerAdapter)

        if(languageSpinnerAdapter.count == 1 || categorySpinnerAdapter.count == 1 || subCategorySpinnerAdapter.count == 1) {
            getData(languageSpinnerAdapter, categorySpinnerAdapter, subCategorySpinnerAdapter)*//*
            recordingThread = Thread(Runnable {
                loadDataAgain() }, "data"
            )
            recordingThread?.start()*//*
        }*/


        attachmentAdapter = AttachmentAdapter(object : RecyclerViewListener {
            override fun recyclerViewListClicked(v: View, position: Int, toInt: Int) {
                val removeItem = (attachedFiles.reversed().toMutableList())[position]
                attachedFiles.remove(removeItem)
                val removeItem1 = (s3AttachedFilesList.reversed().toMutableList()).get(position)
                s3AttachedFilesList.remove(removeItem1)
                attachmentAdapter.setAttachment(attachedFiles.toMutableList())
            }
        })
        rvAttachedFiles.adapter = attachmentAdapter
        rvAttachedFiles.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        etKeywords.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
        loader.dismiss()
        if (isEdit) {
            loader.show()
            articleViewModel.setData(articleLocalId)
            articleViewModel.searchByLiveData?.observe(this, Observer { article ->
                article?.let {
                    globalStatus = article.globalStatus
                    isRework = if (article.status.equals("Rework", ignoreCase = true) ||
                        (article.status.equals("Draft", ignoreCase = true)
                                && article.globalStatus.equals("Rework", ignoreCase = true))) true else false
                    if(article.featuredImage != null && article.featuredImage.isNotEmpty())
                        featuredImage = article.featuredImage
                    etArticleTitle.setText(article.articleTitle, TextView.BufferType.EDITABLE)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        etArticleDescription.setText(Html.fromHtml(article.articleDes, Html.FROM_HTML_MODE_COMPACT), TextView.BufferType.EDITABLE)
                    }
                    else{
                        etArticleDescription.setText(article.articleDes, TextView.BufferType.EDITABLE)
                    }

                    etArticleLocation.setText(article.location, TextView.BufferType.EDITABLE)

                    etKeywords.setText(article.keywords, TextView.BufferType.EDITABLE)

                    selectedLangPosition = article.languageName
                    selectedCatPosition = article.catID
                    tvSelectLanguage.setSelection(languageSpinnerAdapter.getPosition(article.languageName))
                    tvSelectCategory.setSelection(categorySpinnerAdapter.getPosition(article.catID))

                    /* val subCatNam = subCategoryList.filter { it.catId == article.catID }.find { it.id == article.subCatID }
                             ?.subCatName*/
                    selectedSubCatPosition = article.subCatID

                    article.files.forEach {
                        t: String? -> attachment(t,true)
                    }
                    article.s3Files.forEach {
                        t: String? ->
                        if (t != null) {
                            s3AttachedFilesList.add(t)
                        }
                    }
                }
            })
            loader.dismiss()
        }

    }


    fun getData(
        languageSpinnerAdapter: ArrayAdapter<String>,
        categorySpinnerAdapter: ArrayAdapter<String>,
        subCategorySpinnerAdapter: ArrayAdapter<String>
    ) {
        createArticleViewModel.allLanguage.observe(this, Observer { languages ->
            languages?.let {
                languageSpinnerAdapter.clear()
                languageSpinnerAdapter.add("Select Language")
                languageSpinnerAdapter.addAll(it.map { n -> n.languageName })
            }
            if(selectedLangPosition.isNotEmpty())
                tvSelectLanguage.setSelection(languageSpinnerAdapter.getPosition(selectedLangPosition))
        })

        createArticleViewModel.allCategories.observe(this, Observer { categories ->
            categories?.let {
                categorySpinnerAdapter.clear()
                categorySpinnerAdapter.add("Select Category")
                categorySpinnerAdapter.addAll(it.map { n -> n.catName })
            }
            if(selectedCatPosition.isNotEmpty())
                tvSelectCategory.setSelection(categorySpinnerAdapter.getPosition(selectedCatPosition))
        })

        tvSelectCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val id = categoryList.find { it.catName == tvSelectCategory.selectedItem.toString() }?.id
                createArticleViewModel.setData(id ?: 0)
            }
        }

        createArticleViewModel.searchByLiveData?.observe(this, Observer { subCategories ->
            subCategories?.let {
                subCategorySpinnerAdapter.clear()
                subCategorySpinnerAdapter.add("Select Sub-Category")
                subCategorySpinnerAdapter.addAll(it.map { n -> n.subCatName })
            }
            if(selectedSubCatPosition.isNotEmpty())
                spinnerSubCategory.setSelection(subCategorySpinnerAdapter.getPosition(selectedSubCatPosition))
        })

    }
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivVideo ->
                recordVideo()
            R.id.ivAttach ->
                requestPermission()
            R.id.ivVoiceAssistance ->
                recordAudio()
            R.id.ivAttachNormal -> {
                showOrHideAttachIcons()
            }
        }
    }

    private fun showOrHideAttachIcons(){
        val animationShowLayout = AnimationUtils.loadAnimation(this, R.anim.anim_hide_button_layout)
        val animationHideLayout = AnimationUtils.loadAnimation(this, R.anim.anim_show_button_layout)
        obstructor.visibility = View.INVISIBLE
        ivAttachNormal.setImageResource(R.mipmap.attach)
        if (llAttachFiles.visibility == View.VISIBLE &&
            llVideoRecord.visibility == View.VISIBLE &&
            llAudioRecord.visibility == View.VISIBLE
        ) {
            llAttachFiles.visibility = View.GONE
            ivAttach.isClickable = false
            llVideoRecord.visibility = View.GONE
            ivVideo.isClickable = false
            llAudioRecord.visibility = View.GONE
            ivVoiceAssistance.isClickable = false
            llAttachFiles.startAnimation(animationShowLayout)
            llVideoRecord.startAnimation(animationShowLayout)
            llAudioRecord.startAnimation(animationShowLayout)
        } else {
            obstructor.visibility = View.VISIBLE
            ivAttachNormal.setImageResource(R.mipmap.close)
            llAttachFiles.visibility = View.VISIBLE
            ivAttach.isClickable = true
            llVideoRecord.visibility = View.VISIBLE
            ivVideo.isClickable = true
            llAudioRecord.visibility = View.VISIBLE
            ivVoiceAssistance.isClickable = true
            llAttachFiles.startAnimation(animationHideLayout)
            llVideoRecord.startAnimation(animationHideLayout)
            llAudioRecord.startAnimation(animationHideLayout)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.toolbar_create_article_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        var type = ArticleStatus.NONE.ordinal

        when (item?.itemId) {
            R.id.action_save -> type = ArticleStatus.DRAFT.ordinal
            R.id.action_post -> type = ArticleStatus.PENDING.ordinal
        }

        if (item?.itemId == R.id.action_save || item?.itemId == R.id.action_post) {
            if (etArticleTitle.text.isEmpty()) {
                showMsg(this, getString(R.string.article_title_required),Toast.LENGTH_SHORT)
                return false
            }
            else if (tvSelectLanguage.selectedItem == null || tvSelectLanguage.selectedItem.toString().equals("Select Language",ignoreCase = true) ) {
                showMsg(this, "Please Select Language!",Toast.LENGTH_SHORT)
                return false
            }
            else if (tvSelectCategory.selectedItem == null || tvSelectCategory.selectedItem.toString().equals("Select Category",ignoreCase = true) ) {
                showMsg(this, "Please Select Category!",Toast.LENGTH_SHORT)
                return false
            }
            else if ( spinnerSubCategory.selectedItem ==null || spinnerSubCategory.selectedItem.toString().equals("Select Sub-category",ignoreCase = true) ) {
                showMsg(this, "Please Select Sub Category!",Toast.LENGTH_SHORT)
                return false
            }
            else if (etArticleTitle.text.trim().length < 10 ) {
                showMsg(this, "Title should contain minimum of 10 characters!",Toast.LENGTH_SHORT)
                return false
            }
            else if (etArticleDescription.text.trim().isEmpty() ) {
                showMsg(this, "Please provide Article Description!",Toast.LENGTH_SHORT)
                return false
            }
            else if (etArticleLocation.text.trim().isEmpty() ) {
                showMsg(this, "Please provide Location!",Toast.LENGTH_SHORT)
                return false
            }
            //val keywordsArray = optimizedKeywordString(etKeywords.text.toString()).split(",").toTypedArray()
            if(etKeywords.text.toString().trim().isEmpty()){
                showMsg(this, "Please provide keywords!",Toast.LENGTH_SHORT)
                return false
            }
        }

        if (type == ArticleStatus.NONE.ordinal) return false
        loader.show()
        if (isEdit) {
            val article = Article(
                etArticleTitle.text.toString().trim(),
                etArticleDescription.text.toString().trim(),
                tvSelectLanguage.selectedItem.toString(),
                tvSelectCategory.selectedItem.toString(),
                spinnerSubCategory.selectedItem.toString(),
                etArticleLocation.text.trim().toString().trim(),
                if (ArticleStatus.values()[type].caption.equals("DRAFTS",ignoreCase = true)) "Draft"
                else ArticleStatus.values()[type].caption,
                createdTime!!,
                optimizedKeywordString(etKeywords.text.toString()),
                attachedFiles.toMutableList(), s3AttachedFilesList.toMutableList(), featuredImage, articleId.toString(),"",
                "",globalStatus,
                modifiedTime!!
            )
            article.localId = articleLocalId
            if(ArticleStatus.values()[type] == ArticleStatus.DRAFT) {
                createArticleViewModel.updateArticle(article,true)
                /*createArticleViewModel.updateDraft(
                    articleLocalId,
                    tvSelectLanguage.selectedItem.toString(),
                    tvSelectCategory.selectedItem.toString(),
                    spinnerSubCategory.selectedItem.toString(),
                    etArticleLocation.text.trim().toString().trim(),
                    etArticleTitle.text.trim().toString().trim(),
                    etArticleDescription.text.toString().trim(),
                    optimizedKeywordString(etKeywords.text.toString()),
                    attachedFiles.toMutableList(),
                    s3AttachedFilesList.toMutableList(),
                    if (ArticleStatus.values()[type].caption.equals("DRAFTS",ignoreCase = true)) "Draft"
                    else ArticleStatus.values()[type].caption,
                    featuredImage,
                    System.currentTimeMillis()
                )*/
                showMsg(application, "Article Updated Successfully", Toast.LENGTH_SHORT)
                loader.dismiss()
                finish()
            }
            if (ArticleStatus.values()[type] != ArticleStatus.DRAFT) {
                if(isRework){
                    commentsDialog(article).show()
                    /*if (m_Text.trim().isNullOrEmpty()){
                        loader.dismiss()
                        return false
                    }
                    else{
                        article.comment = m_Text
                    }*/
                    //article.comment = m_Text
                }
                else {
                    loader.show()
                    createArticleViewModel.updateArticle(article, true)
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
            }

        } else {
            val article = Article(
                etArticleTitle.text.toString().trim(),
                etArticleDescription.text.toString().trim(),
                tvSelectLanguage.selectedItem.toString(),
                tvSelectCategory.selectedItem.toString(),
                spinnerSubCategory.selectedItem.toString(),
                etArticleLocation.text.trim().toString().trim(),
                if(ArticleStatus.values()[type].caption.equals("DRAFTS",ignoreCase = true)) "Draft" else ArticleStatus.values()[type].caption,
                System.currentTimeMillis(),
                optimizedKeywordString(etKeywords.text.toString()),
                attachedFiles.toMutableList(), s3AttachedFilesList.toMutableList(),"",articleId.toString(),
                "","",globalStatus,
                System.currentTimeMillis()

            )

            createArticleViewModel.createArticle(article)
            if (ArticleStatus.values()[type] != ArticleStatus.DRAFT) {
                showMsg(application, "Article Saved Successfully", Toast.LENGTH_SHORT)
            }
            if (ArticleStatus.values()[type] == ArticleStatus.DRAFT) {
                //createArticleViewModel.insert(article)
                showMsg(application, "Article Created Successfully", Toast.LENGTH_SHORT)
            }

            loader.dismiss()
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    private var m_Text = ""
    private fun commentsDialog(article: Article):android.support.v7.app.AlertDialog.Builder {
        val builder = android.support.v7.app.AlertDialog.Builder(this)
        val li = LayoutInflater.from(this);
        val v = li.inflate(R.layout.dialog_comments, null)
        builder.setView(v);
        val userInput = v.findViewById<EditText>(R.id.input);
        builder.setPositiveButton(R.string.submit) { dialog, which ->
            dialog.cancel()
            m_Text = userInput.text.toString()
            article.comment = m_Text
            loader.show()
            createArticleViewModel.updateArticle(article, true)
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


    private fun optimizedKeywordString(keywordString: String): String {
        val optimised = keywordString.trim()
        var splitted = optimised.split(",")
        splitted = splitted.map { it.trim() }
        splitted = splitted.filter { it.isNotEmpty() && it != "." }
        var final = ""
        for ((index, word) in splitted.withIndex()) {
            final += if (index < splitted.size - 1) "$word," else word
        }
        return final
    }


    val a = Array<String>(size = 2, init = { index: Int ->
        Manifest.permission.READ_EXTERNAL_STORAGE;
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    })

    fun requestPermission() = if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            this, a,
            1
        );
    } else {
        selectFile()
    }


    private fun selectFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            startActivityForResult(
                Intent.createChooser(intent, resources.getString(R.string.select_a_file_to_upload)),
                PICK_FILE_RESULT_CODE
            )
        } catch (ex: ActivityNotFoundException) {
            showMsg(this,getString(R.string.please_install_a_file_manager),Toast.LENGTH_SHORT)
        }
    }


    fun getS3Url(): String {
        val bucketName =
            AWSMobileClient.getInstance().configuration.optJsonObject("S3TransferUtility").getString("Bucket")
        val endPoint = s3Client?.endpoint.toString().replace("https://", ".")
        return "https://" + bucketName + endPoint
    }

    private fun uploadAttachment(fileUri: Uri, date: String, isRecordedAudio: Boolean, imageFile: File?) {

        var s:String?=null
        var file:File?=null
        var folder = ""
        if(imageFile != null){
            s = profileImageURL+".png"
            file = imageFile
            folder = "images/"
        }
        else {
            if (isRecordedAudio) {
                s = fileUri.lastPathSegment
                file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "/$s")
                folder = "audio_mic/"
            } else {
                s = fileUri.lastPathSegment + "_" + date + "." + getFileExtension(fileUri)
                file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/$s")
                val attachmentExtensions = getFileExtension(fileUri)
                if (attachmentExtensions.equals("mp4", ignoreCase = true) || attachmentExtensions.equals(
                        "3gpp",
                        ignoreCase = true
                    ) ||
                    attachmentExtensions.equals("3gp", ignoreCase = true) || attachmentExtensions.equals(
                        "avi",
                        ignoreCase = true
                    )
                ) {
                    folder = "video/"

                } else if (attachmentExtensions.equals("mp3", ignoreCase = true)) {
                    folder = "audio_mic/"
                } else if (attachmentExtensions.equals("jpeg", ignoreCase = true) ||
                    attachmentExtensions.equals("png", ignoreCase = true) ||
                    attachmentExtensions.equals("jpg", ignoreCase = true)
                ) {
                    folder = "images/"
                } else {
                    showMsg(this, "Something wrong", Toast.LENGTH_SHORT)
                    return
                }
            }

        //val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/$s")

        createFile(applicationContext, fileUri, file)

    }
        val transferUtility = TransferUtility.builder()
            .context(applicationContext)
            .awsConfiguration(AWSMobileClient.getInstance().configuration)
            .s3Client(s3Client)
            .build()



        val uploadObserver =

            transferUtility.upload("DRAFTS/" + folder + s, file, CannedAccessControlList.PublicRead)

        uploadObserver.setTransferListener(object : TransferListener {

            override fun onStateChanged(id: Int, state: TransferState) {
                if (TransferState.COMPLETED === state) {
                    showMsg(applicationContext,"Upload Completed!",Toast.LENGTH_SHORT)
                    val objectUrl = getS3Url() + "/" + uploadObserver.key
                    s3AttachedFilesList.add(objectUrl)
                    attachment(uploadObserver.key, false)
                    file.delete()

                } else if (TransferState.FAILED === state) {
                    file.delete()
                    showMsg(applicationContext, "Upload Failed!", Toast.LENGTH_SHORT)
                    loader.dismiss()

                }

            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                val percentDonef = bytesCurrent.toFloat() / bytesTotal.toFloat() * 100
                val percentDone = percentDonef.toInt()

                "ID:$id|bytesCurrent: $bytesCurrent|bytesTotal: $bytesTotal|$percentDone%"
            }

            override fun onError(id: Int, ex: Exception) {
                ex.printStackTrace()
            }

        })
    }

    private fun createFile(context: Context, srcUri: Uri, dstFile: File) {
        try {
            val inputStream = context.contentResolver.openInputStream(srcUri) ?: return
            val outputStream = FileOutputStream(dstFile)
            IOUtils.copy(inputStream, outputStream)
            inputStream.close()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun getFileExtension(uri: Uri): String? {
        val contentResolver = contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver.getType(uri))
    }

    @AfterPermissionGranted(RC_AUDIO_AND_STORAGE)
    private fun recordAudio() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        //ActivityCompat.requestPermissions(this, permissions, 0)
        if (EasyPermissions.hasPermissions(this, *permissions)) {
            /*try {
                val myIntent = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
                myIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30)
                startActivityForResult(myIntent, RECORD_SOUND_RESULT_CODE)
            } catch (ex: ActivityNotFoundException) {*/
                audioRecordDialog(R.layout.dialog_audio_record, this).show()
            //}
        } else {
            EasyPermissions.requestPermissions(
                this, getString(R.string.camera_and_location_rationale),
                RC_AUDIO_AND_STORAGE, *permissions
            )
        }
    }

    private fun audioRecordDialog(layout: Int, context: Context): Dialog {
        val dialog = customDialog(layout, context)
        val chronometer = dialog.findViewById<Chronometer>(R.id.chronometer)
        dialog.setCanceledOnTouchOutside(true);
        chronometer.stop()
        val tvRecord = dialog.findViewById<TextView>(R.id.tvRecord)
        val tvStop = dialog.findViewById<TextView>(R.id.tvStop)
        tvRecord.isEnabled = true
        tvStop.isEnabled = false
        if(Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            tvRecord.setBackgroundResource(R.drawable.dark_blue_rounded_corners_filled);
        } else {
            tvRecord.background = (ContextCompat.getDrawable(context, R.drawable.dark_blue_rounded_corners_filled));
        }
        if(Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            tvStop.setBackgroundResource(R.drawable.dark_grey_rounded_corners_filled);
        } else {
            tvStop.background = (ContextCompat.getDrawable(context, R.drawable.dark_grey_rounded_corners_filled));
        }
        tvRecord.setOnClickListener {
            if(Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                tvRecord.setBackgroundResource(R.drawable.dark_grey_rounded_corners_filled);
            } else {
                tvRecord.background = (ContextCompat.getDrawable(context, R.drawable.dark_grey_rounded_corners_filled));
            }
            tvRecord.isEnabled = false
            if(Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                tvStop.setBackgroundResource(R.drawable.dark_blue_rounded_corners_filled);
            } else {
                tvStop.background = (ContextCompat.getDrawable(context, R.drawable.dark_blue_rounded_corners_filled));
            }
            tvStop.isEnabled = true
            dialog.setCanceledOnTouchOutside(false);
            startRecording()
            chronometer.base = SystemClock.elapsedRealtime();
            chronometer.start()
            isRecordingStarted = true
        }
        dialog.findViewById<TextView>(R.id.tvStop).setOnClickListener {
            stopRecording()
            dialog.dismiss()
            chronometer.stop()
            isRecordingStarted = false
        }
        return dialog
    }

    var audioFilePath = ""
    private var isRecordingStarted = false

    private fun startRecording() {
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder.setAudioChannels(1);
        audioFilePath = getAudioFileName()
        mediaRecorder.setOutputFile(audioFilePath)
        mediaRecorder.prepare()
        mediaRecorder.start()
    }

    private fun stopRecording() {
        if (isRecordingStarted) {
            mediaRecorder.stop()
            loader.show()
            uploadAttachment(Uri.parse(audioFilePath),"",true, null)
            //attachment(audioFilePath)
        }
    }

    @AfterPermissionGranted(RC_CAMERA_AND_LOCATION)
    fun recordVideo() {
        val perms = arrayOf(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            val takeVideoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val photoFile = createFile()
                if (photoFile != null) {
                    startActivityForResult(takeVideoIntent, RECORD_VIDEO_RESULT_CODE)
                }

            /*val takeVideoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)*/
        } else {
            EasyPermissions.requestPermissions(
                this, getString(R.string.camera_and_location_rationale),
                RC_CAMERA_AND_LOCATION, *perms
            )
        }
    }

    private var profileImageURL: String = ""
    var mCurrentPhotoPath: String? = null
    var profileImage: File? = null
    private fun getMediaDirectory(): File {
        val mediaDirectory =
            File(applicationContext.filesDir.toString() + "/" + resources.getString(R.string.app_name) + "/")
        if (!mediaDirectory.exists())
            mediaDirectory.mkdirs()
        return mediaDirectory
    }

    private fun createFile(): File? {
        getMediaDirectory()
        val simpleDateFormat = SimpleDateFormat("dd-mm-yyyy-hh-mm-ss")
        val date = simpleDateFormat.format(Date(System.currentTimeMillis()))
        profileImageURL = "image" + ":" + date
        val storageDir =
            File(Environment.getExternalStorageDirectory().toString() + "/" + resources.getString(R.string.app_name))
        //val filesDir = applicationContext.getFilesDir()
        if (!storageDir.exists())
            storageDir.mkdir()
        var image: File? = null
        try {
            image = File.createTempFile(profileImageURL, ".png", storageDir)
        } catch (e: IOException) {
        }
        mCurrentPhotoPath = image?.absolutePath
        profileImage = image
        return image
    }

    private fun attachment(file: String?, isEdit: Boolean) {
        val files = attachedFiles
        file?.let { files.add(it) }
        attachmentAdapter.setAttachment(files.toMutableList())
        loader.dismiss()
        if(!isEdit)
        showOrHideAttachIcons()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE -> {
                showMsg(this, getString(R.string.returned_from_app_settings_to_activity), Toast.LENGTH_SHORT)
            }
            PICK_FILE_RESULT_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    handleReturn(data?.data)
                }
                if (bitmap != null) {
                    bitmap.recycle();
                }

                if (requestCode == CHOOSING_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
                    fileUri = data.data
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, fileUri);
                    } catch (e: IOException) {
                        e.printStackTrace();
                    }
                }
            }
            RECORD_VIDEO_RESULT_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    //handleReturn(data?.data)
                    if (data != null) {
                        var uri = data.data
                        if(data.data == null) {
                            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                                uri = data.data
                            } else {
                                val photo: Bitmap = data.getExtras().get("data") as Bitmap;
                                val bytes = ByteArrayOutputStream()
                                val path = MediaStore.Images.Media.insertImage(getContentResolver(), photo, "image_"+System.currentTimeMillis(), null);
                                uri = Uri.parse(path)
                            }
                        }
                        var file: File? = null
                        if (uri != null) {
                            val bitmap = getBitmapFromUri(uri)
                            file = persistImage(bitmap, profileImageURL)
                        } else {
                            if (profileImage != null)
                                file = profileImage
                        }
                        if (file != null) {
                            loader.show()
                            val date = simpleDateFormat.format(Date(System.currentTimeMillis()))
                            uploadAttachment(Uri.fromFile(file), date, false, file)
                        }
                    }
                }
            }
            RECORD_SOUND_RESULT_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    handleReturn(data?.data)
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun persistImage(bitmap: Bitmap, name: String): File {
        val filesDir = applicationContext.getFilesDir()
        val imageFile = File(filesDir, "$name.png")

        val os: OutputStream
        try {
            os = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
            os.flush()
            os.close()
        } catch (e: Exception) {
            Log.e(javaClass.getSimpleName(), "Error writing bitmap", e)
        }
        return imageFile
    }
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor?.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
    }

    private fun handleReturn(uri: Uri?) {
        if (uri == null) return
        if (uri.path != null) {
            loader.show()
            val date = simpleDateFormat.format(Date(System.currentTimeMillis()))
            uploadAttachment(uri,date,false,null)
        } else
            showMsg(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private var dialogClickListener: DialogInterface.OnClickListener =
        DialogInterface.OnClickListener { _, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    finish()
                }

                DialogInterface.BUTTON_NEGATIVE -> {

                }
            }
        }

   override fun onBackPressed() {
       val builder = android.support.v7.app.AlertDialog.Builder(this)
       builder.setMessage("Are you sure you want to exit?").setPositiveButton(R.string.yes, dialogClickListener)
           .setNegativeButton(R.string.no, dialogClickListener).show()

}
}

