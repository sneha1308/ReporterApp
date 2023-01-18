package com.example.reporterapp.activity

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobile.client.AWSMobileClient
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.bumptech.glide.Glide
import com.example.reporterapp.*
import com.example.reporterapp.callback.LoginResponse
import com.example.reporterapp.database.table.User
import com.example.reporterapp.viewmodel.UserViewModel
import de.hdodenhof.circleimageview.CircleImageView
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.*

class ProfileActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var loader: AlertDialog

    private var PERMISSIONS_IMAGE_CHOOSER = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private var REQUEST_PERMISSION_IMAGE_CHOOSER = 2
    private var REQUEST_IMAGE_CHOOSER = 3
    private var REQUEST_IMAGE_CAPTURE = 4
    var mCurrentPhotoPath: String? = null
    var profileImage: File? = null

    private lateinit var userViewModel: UserViewModel
    lateinit var location: EditText
    lateinit var mobileNumber: EditText
    var user: User? = null
    private var s3Client: AmazonS3Client? = null
    private val KEY = "AKIAXDCDWHJZBNJJHHF5"
    private val SECRET = "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"
    private var credentials: BasicAWSCredentials? = null
    var linkedInUrl = ""
    var twitterUrl = ""

    private var profileImageURL: String = ""


    var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loader = getAlertDialog(this)
        loader.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        loader.show()

        val progressRunnable = object : Runnable {
            override fun run() {
                if(loader.isShowing) {
                    showMsg(
                        this@ProfileActivity,
                        "Timeout! please check your internet connection",
                        Toast.LENGTH_SHORT
                    )
                    loader.dismiss()
                }
            }

        }
    val pdCanceller = Handler();
    pdCanceller.postDelayed(progressRunnable, 20000);

        setContentView(R.layout.activity_profile)
        supportActionBar?.title = resources.getString(R.string.my_profile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        userViewModel = ViewModelProviders.of(this).get(UserViewModel::class.java)
        if (isOnline(application)) {
            userViewModel.fetchData()
            userViewModel.setData()
        } else {
            showMsg(getApplication(),"Please check Internet Connection",Toast.LENGTH_SHORT)
            return
        }
        userViewModel.getData()
        //user = userViewModel.searchByLiveData


        AWSMobileClient.getInstance().initialize(this).execute()
        credentials = BasicAWSCredentials(KEY, SECRET)
        s3Client = AmazonS3Client(credentials)

        findViewById<ImageView>(R.id.profile_user_imageView).setOnClickListener(this)
        findViewById<TextView>(R.id.tvReporterAccount).setOnClickListener(this)
        findViewById<TextView>(R.id.tvReporterContactInfo).setOnClickListener(this)
        findViewById<TextView>(R.id.tvReporterSocialLinks).setOnClickListener(this)

        //mPostLoginViewModel = PostLoginViewModel()
        // mPostLoginViewModel.getProfileDetails(userViewModel)
        userViewModel.searchByLiveData?.observe(this, Observer { user ->
            user?.let {
                setUserData(user)
                loader.dismiss()
            }
        })
    }

    /*  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
            val inflater = menuInflater
            inflater.inflate(R.menu.toolbar_profile, menu)
            return super.onCreateOptionsMenu(menu)
        }

        override fun onOptionsItemSelected(item: MenuItem?): Boolean {
            when (item?.itemId) {
                R.id.action_save ->
                    startActivity(Intent(this, MainActivity::class.java))
            }
            return super.onOptionsItemSelected(item)

        }*/

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.profile_user_imageView -> {
                dispatchImageChooserIntent()
            }
            R.id.tvReporterAccount -> {
                Log.e("Profile", "click")
                updatePasswordDialog(R.layout.dialog_account_info, this).show()
            }
            R.id.tvReporterContactInfo -> {
                Log.e("Profile", "clicked contact")

                user?.let {
                    updateContactInfo(R.layout.dialog_contact_info, this, it).show()
                }
            }
            R.id.tvReporterSocialLinks -> {
                user?.let { updateLinks(R.layout.dialog_update_social_links, this, it).show() }
            }
        }
    }

    fun setUserData(user: User?) {
        this.user = user
        if (user != null) {
            if (user.isVerifiedByAdmin) {
                findViewById<TextView>(R.id.inActiveStatus).visibility = View.GONE
            } else {
                findViewById<TextView>(R.id.inActiveStatus).visibility = View.VISIBLE
            }
        }
        findViewById<TextView>(R.id.tvDisplayUserName).text = user?.name
        findViewById<TextView>(R.id.etDisplayLocation).text = user?.location
        findViewById<TextView>(R.id.etPhoneNumber).text = user?.mobilenumber
        findViewById<TextView>(R.id.tvDisplayLinkedInLink).text = user?.linkedin
        findViewById<TextView>(R.id.tvDisplayTwitterLink).text = user?.twitter
        if(user?.loginTime != null)
            findViewById<TextView>(R.id.tvDisplayLastLogin).text = getLastLoginString(user?.loginTime)
        else
            findViewById<TextView>(R.id.tvDisplayLastLogin).text = "-"
        Glide.with(this.baseContext).load(user?.profile_Photo).centerCrop().placeholder(R.drawable.user_place_holder_gen_list)
            .into(findViewById(R.id.profile_user_imageView))
    }

    fun updatePasswordDialog(layout: Int, context: Context): Dialog {
        val dialog = customDialog(layout, context)
        (dialog.findViewById(R.id.tvSavePassword) as TextView).setOnClickListener {
            val oldPass = (dialog.findViewById(R.id.etOldPassword)as? EditText)?.text.toString()
            val newPass = (dialog.findViewById(R.id.etEnterNewPassword)as? EditText)?.text.toString()
            val newPassConfirm = (dialog.findViewById(R.id.etEnterConfirmPassword)as? EditText)?.text.toString()
            if (oldPass.isEmpty() || newPass.isEmpty() || newPassConfirm.isEmpty()) {
                showMsg(context,"Please fill the details",Toast.LENGTH_SHORT)
            }
            else if(!newPass.equals(newPassConfirm)){
                showMsg(context,"Confirm Password mismatch with the new password",Toast.LENGTH_SHORT)
            }
            else if(oldPass.equals(newPass)){
                showMsg(context,"New password should not be same as old password.",Toast.LENGTH_SHORT)
            }
            else if(!Validator.isValidPassword(newPass)) {
                showMsg(
                    context,
                    "Try using a minimum of 8 characters and a mixture of UPPERCASE, lowercase, digits(0-9) & special characters",
                    Toast.LENGTH_SHORT
                )
            }
            else {
                loader.show()
                sendDetailsToServer(oldPass,newPass,object : LoginResponse {

                    override fun displayError(message: String) {
                        loader.dismiss()
                        showMsg(context,message,Toast.LENGTH_SHORT)
                    }
                    override fun success() {
                        setUserData(user)
                        loader.dismiss()
                        showMsg(context,"Your password has been successfully changed",Toast.LENGTH_SHORT)
                        clearDataAndLogout()
                        dialog.dismiss()
                    }

                    override fun fail(e:String) {
                        loader.dismiss()
                        showMsg(context,e,Toast.LENGTH_SHORT)
                    }
                })


                hideKeyboard(this)

            }
        }
        (dialog.findViewById(R.id.tvCancelDialog) as TextView).setOnClickListener {
            dialog.dismiss()
        }
        return dialog
    }

    fun sendDetailsToServer(oldPass: String, newPass: String, callBackResponse: LoginResponse){
        userViewModel.changepassword(oldPass, newPass, callBackResponse)
    }

    private fun clearDataAndLogout() {
        //com.example.reporterapp.app.PreferenceManager.getInstance(this).clearToken()
        finishAffinity()
        intent = Intent(this, SignInSignUpActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("clearData",true)
        startActivity(intent)
        loader.dismiss()
    }

    fun updateContactInfo(layout: Int, context: Context, user: User): Dialog {
        val dialog = customDialog(layout, context)
        if (user != null) {
            (dialog.findViewById(R.id.etLocation) as EditText).text =
                Editable.Factory.getInstance().newEditable(user.location)
            (dialog.findViewById(R.id.etEnterPhoneNumber) as EditText).text =
                Editable.Factory.getInstance().newEditable(user.mobilenumber)
        }
        (dialog.findViewById(R.id.tvSavePassword) as TextView).setOnClickListener {
            val loc = (dialog.findViewById(R.id.etLocation) as EditText).text.toString()
            val mobile = (dialog.findViewById(R.id.etEnterPhoneNumber) as EditText).text.toString()
            if (loc == null || mobile == null || loc.trim().isEmpty() || mobile.trim().isEmpty()) {
                showMsg(context,"Please fill the details",Toast.LENGTH_SHORT)
            } else if(mobile.length < 4){
                showMsg(context,"Invalid landline",Toast.LENGTH_SHORT)
            }else {
                user.location = loc
                user.mobilenumber = mobile
                loader.show()
                userViewModel.updateContactInfo(user)
                setUserData(user)
                showMsg(context,"Updated successfully",Toast.LENGTH_SHORT)
                dialog.dismiss()
                hideKeyboard(this)
                loader.dismiss()
            }
        }
        (dialog.findViewById(R.id.tvCancelDialog) as TextView).setOnClickListener {
            dialog.dismiss()
        }
        return dialog
    }

    fun updateLinks(layout: Int, context: Context, user: User): Dialog {
        val dialog = customDialog(layout, context)
        if (user != null) {
            (dialog.findViewById(R.id.etAddLinkedInLink) as EditText).text =
                Editable.Factory.getInstance().newEditable(user.linkedin)
            (dialog.findViewById(R.id.etAddTwitterLink) as EditText).text =
                Editable.Factory.getInstance().newEditable(user.twitter)
        }
        (dialog.findViewById(R.id.tvSavePassword) as TextView).setOnClickListener {
            linkedInUrl = (dialog.findViewById(R.id.etAddLinkedInLink) as EditText).text.toString()
            twitterUrl = (dialog.findViewById(R.id.etAddTwitterLink) as EditText).text.toString()
            if ((linkedInUrl == null && twitterUrl == null) || (linkedInUrl.isEmpty() && twitterUrl.isEmpty())) {
                showMsg(context,"Please fill the details",Toast.LENGTH_SHORT)
                hideKeyboard(this)
                return@setOnClickListener
            }
            if(linkedInUrl.isNotEmpty()){
                if(!linkedInUrl.contains("http", ignoreCase = true)) {
                    showMsg(context, "Invalid LinkedIn link", Toast.LENGTH_SHORT)
                    hideKeyboard(this)
                    return@setOnClickListener
                }
            }
            if(twitterUrl.isNotEmpty()){
                if(!twitterUrl.contains("http", ignoreCase = true)) {
                    showMsg(context, "Invalid Twitter link", Toast.LENGTH_SHORT)
                    hideKeyboard(this)
                    return@setOnClickListener
                }
            }
            if(linkedInUrl.isNotEmpty() || twitterUrl.isNotEmpty()) {
                user.linkedin = linkedInUrl
                user.twitter = twitterUrl
                loader.show()
                userViewModel.updateLinks(user)
                setUserData(user)
                showMsg(context,"Updated successfully",Toast.LENGTH_SHORT)
                dialog.dismiss()
                hideKeyboard(this)
                loader.dismiss()
            }
        }
        (dialog.findViewById(R.id.tvCancelDialog) as TextView).setOnClickListener {
            dialog.dismiss()
        }
        return dialog
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CHOOSER) {
                if (data != null) {
                    var uri = data?.data
                    if(data.data == null) {
                        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                            uri = data.data
                        } else {
                            val photo: Bitmap = data.getExtras().get("data") as Bitmap;
                            val bytes = ByteArrayOutputStream()
                            val path = MediaStore.Images.Media.insertImage(getContentResolver(), photo, "profle_picture_"+System.currentTimeMillis(), null);
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
                        uploadAttachment(file)
                    }
                }
            }
        } else {
            return
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (!(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            return
        }
        when (requestCode) {
            REQUEST_PERMISSION_IMAGE_CHOOSER -> dispatchImageChooserIntent()
        }
    }

    private fun createFile(): File? {
        getMediaDirectory()
        profileImageURL = resources.getString(R.string.app_name) + "-" + System.currentTimeMillis()
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

    private fun getMediaDirectory(): File {
        val mediaDirectory =
            File(applicationContext.filesDir.toString() + "/" + resources.getString(R.string.app_name) + "/")
        if (!mediaDirectory.exists())
            mediaDirectory.mkdirs()
        return mediaDirectory
    }


    val a = Array<String>(size = 2, init = { index: Int ->
        Manifest.permission.READ_EXTERNAL_STORAGE
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    })

    @AfterPermissionGranted(RC_CAMERA_AND_LOCATION)
    private fun dispatchImageChooserIntent() {

        val perms = arrayOf(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            val takeVideoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val pickImageIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageIntent.putExtra("pick", true)
            val chooserIntent = Intent.createChooser(pickImageIntent, "Choose Action")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf<Intent>(takeVideoIntent))
            if(pickImageIntent.resolveActivity(getPackageManager()) != null) {
                val photoFile = createFile()
                if (photoFile != null) {
                    startActivityForResult(chooserIntent, REQUEST_IMAGE_CHOOSER)
                } else {
                    startActivityForResult(pickImageIntent, REQUEST_IMAGE_CHOOSER)
                }
            }
            else{
                startActivityForResult(pickImageIntent, REQUEST_IMAGE_CHOOSER)
            }

            /*val takeVideoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)*/
        } else {
            EasyPermissions.requestPermissions(
                this, getString(R.string.camera_and_location_rationale),
                RC_CAMERA_AND_LOCATION, *perms
            )
        }

     /*   val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_IMAGE_CHOOSER,
                REQUEST_PERMISSION_IMAGE_CHOOSER
            )
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
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
            val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageIntent.putExtra("pick", true)

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.putExtra("pick", false)
            val pickTitle = "Choose Action"
            val chooserIntent = Intent.createChooser(pickImageIntent, pickTitle)
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf<Intent>(takePictureIntent))

            if (takePictureIntent.resolveActivity(packageManager) != null) {
                val photoFile = createFile()
                if (photoFile != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                    startActivityForResult(chooserIntent, REQUEST_IMAGE_CHOOSER)
                } else {
                    startActivityForResult(pickImageIntent, REQUEST_IMAGE_CHOOSER)
                }
            } else {
                startActivityForResult(pickImageIntent, REQUEST_IMAGE_CHOOSER)
            }

        }*/
    }

    fun getS3Url(): String {
        val bucketName =
            AWSMobileClient.getInstance().configuration.optJsonObject("S3TransferUtility").getString("Bucket")
        val endPoint = s3Client?.endpoint.toString().replace("https://", ".")
        return "https://" + bucketName + endPoint
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
        val fileDescriptor = parcelFileDescriptor?.fileDescriptor
        val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
        parcelFileDescriptor.close()
        return image
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

    private fun uploadAttachment(file: File) {

        val folder = "Profile-Pictures/"


        val transferUtility = TransferUtility.builder()
            .context(applicationContext)
            .awsConfiguration(AWSMobileClient.getInstance().configuration)
            .s3Client(s3Client)
            .build()
            //val bitmap:Bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(filePath));
        //val file = File(filePath?.path)
        val uploadObserver =

            transferUtility.upload(
                folder + profileImageURL + ".png",
                file,
                CannedAccessControlList.PublicRead
            )


        uploadObserver.setTransferListener(object : TransferListener {

            override fun onStateChanged(id: Int, state: TransferState) {
                if (TransferState.COMPLETED === state) {
                    showMsg(applicationContext,"Profile Picture Updated Successfully",Toast.LENGTH_SHORT)
                    val objectUrl = getS3Url() + "/" + uploadObserver.key
                    user!!.profile_Photo = objectUrl
                    userViewModel.updateProfilePhoto(user!!)
                    Glide.with(applicationContext).load(objectUrl).centerCrop()
                        .into(findViewById<CircleImageView>(R.id.profile_user_imageView))
                    file.delete()
                    loader.dismiss()

                } else if (TransferState.FAILED === state) {
                    file.delete()
                    showMsg(applicationContext,"Upload Failed!",Toast.LENGTH_SHORT)
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
}
