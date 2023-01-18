package com.example.reporterapp.activity

import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.example.reporterapp.R
import com.example.reporterapp.adapter.SignInSignUpPagerAdapter
import com.example.reporterapp.callback.IRegister
import com.example.reporterapp.callback.LoginResponse
import com.example.reporterapp.getAlertDialog
import com.example.reporterapp.isOnline
import com.example.reporterapp.showMsg
import com.example.reporterapp.viewmodel.PreLoginViewModel
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.squareup.okhttp.*
import com.squareup.okhttp.Callback
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import kotlinx.android.synthetic.main.activity_signin_signup.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

const val RC_SIGN_IN = 1024

/*
* This activity creates a UI in a single window for signup and signin screen as Tabs
* This class handles the general login and social login functionality.
* */
class SignInSignUpActivity : AppCompatActivity(), View.OnClickListener , GoogleApiClient.OnConnectionFailedListener{
    override fun onConnectionFailed(p0: ConnectionResult) {
        showMsg(this@SignInSignUpActivity, "Failed", Toast.LENGTH_SHORT)
    }

    val RC_SIGN_IN: Int = 1

    private lateinit var firebaseAuth: FirebaseAuth

    lateinit var loader: AlertDialog

    private lateinit var callbackManager: CallbackManager
    private var mTwitterAuthClient = TwitterAuthClient()
    private lateinit var gso: GoogleSignInOptions
    private lateinit var mGoogleSignInClient: GoogleApiClient
    private lateinit var mPreLoginViewModel: PreLoginViewModel

    /*
    * This method is called when the application is launched and about to show the screen to the user
    * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin_signup)
        supportActionBar?.hide()

        val isSignout = intent.getBooleanExtra("clearData", false)


        val ivFacebook= findViewById<ImageView>(R.id.ivFacebook)
        val ivTwitter= findViewById<ImageView>(R.id.ivTwitter)
        val ivGoogle= findViewById<ImageView>(R.id.ivGoogle)
        ivFacebook.setOnClickListener(this)
        ivTwitter.setOnClickListener(this)
        ivGoogle.setOnClickListener(this)

        com.example.reporterapp.app.PreferenceManager.getInstance(this).clearToken()
        com.example.reporterapp.app.PreferenceManager.getInstance(this).clearRefreshDate()
        mPreLoginViewModel = ViewModelProviders.of(this).get(PreLoginViewModel::class.java)

        loader = getAlertDialog(this)
        loader.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val progressRunnable = object : Runnable {
            override fun run() {
                if(loader.isShowing) {
                    showMsg(
                        this@SignInSignUpActivity,
                        "Timeout! please check your internet connection",
                        Toast.LENGTH_SHORT
                    )
                    loader.dismiss()
                }
            }

        }
        val pdCanceller = Handler();
        pdCanceller.postDelayed(progressRunnable, 20000);

        tabLayout.addTab(tabLayout.newTab().setText(R.string.sign_in))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.sign_up))

        viewPager.adapter = SignInSignUpPagerAdapter(this, supportFragmentManager, tabLayout.tabCount)
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabReselected(tab: TabLayout.Tab?) {

                if (tab != null && tab.position == 1) {
                    //tab.select()
                    viewPager.currentItem = -1
                    onTabSelected(tab)
                    viewPager.isSelected = true
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {

                if (tab != null){
                    if(viewPager.currentItem == -1)
                        tabLayout.setScrollPosition(tab.position,0f,false);
                    viewPager.currentItem = tab.position
                    tabLayout.setScrollPosition(tab.position,0f,true);

                }
            }
        })

        firebaseAuth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestServerAuthCode(resources.getString(R.string.web_application_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleApiClient.Builder(this)
            .enableAutoManage(this, this as GoogleApiClient.OnConnectionFailedListener)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()

        mGoogleSignInClient.connect()
        if(isSignout){
            if (mGoogleSignInClient.isConnected()) {
                Auth.GoogleSignInApi.signOut(mGoogleSignInClient);
                mGoogleSignInClient.disconnect();
                mGoogleSignInClient.connect();
            }
            LoginManager.getInstance().logOut();
        }

        val config = TwitterConfig.Builder(this)
            .logger(DefaultLogger(Log.DEBUG))//enable logging when app is in debug mode
            .twitterAuthConfig(
                TwitterAuthConfig(
                    resources.getString(R.string.CONSUMER_KEY),
                    resources.getString(R.string.CONSUMER_SECRET)
                )
            )//pass the created app Consumer KEY and Secret also called API Key and Secret
            .debug(true)//enable debug mode
            .build()

        Twitter.initialize(config);
    }

    /*This method is called when clicking on the button
    * @param v is the view on which the user is clicking */
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivFacebook -> {
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email"))
                registerThroughLoginManager()
            }
            R.id.ivGoogle -> {
                /*var account = GoogleSignIn.getLastSignedInAccount(this)
                if (account == null || account.idToken == null) {*/
                    signIn()
                /*} else {
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        putExtra("token", account.idToken)
                        putExtra("localTime", "")
                    })
                }*/
            }
            R.id.ivTwitter -> {
                mTwitterAuthClient.authorize(this, object : com.twitter.sdk.android.core.Callback<TwitterSession>() {
                    override fun success(result: Result<TwitterSession>?) {
                        twitterSuccess()
                    }

                    override fun failure(e: TwitterException) {
                        e.printStackTrace()
                    }
                })
            }
        }

    }


    /*This method is called to login with the google signIn when the access token is not null */
    private fun signIn() {
        if(mGoogleSignInClient.isConnected)
            Auth.GoogleSignInApi.signOut(mGoogleSignInClient);
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    /*This method is called to access the facebook token and do the operations based on the status */
    private fun registerThroughLoginManager() {
        //val accessToken = AccessToken.getCurrentAccessToken()
        //   val isLoggedIn = accessToken != null && !accessToken.isExpired
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    showMsg(this@SignInSignUpActivity, "success", Toast.LENGTH_SHORT)
                    facebookSuccess()
                }

                override fun onCancel() {
                }

                override fun onError(exception: FacebookException) {
                    showMsg(this@SignInSignUpActivity, resources.getString(R.string.onError) + "$exception", Toast.LENGTH_SHORT)
                }
            })
    }

    /*This method is called when the callback is success and
    send the access token to server and navigates to MainActivity(Dashboard screen)*/
    fun facebookSuccess() {
        loader.show()
        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired
        if (isLoggedIn) {
            accessToken?.let { it1 -> socialAuthentication(it1.token, "facebook") }
        }else {
            showMsg(this, "Facebook login failed!", Toast.LENGTH_SHORT)
        }
    }


    fun twitterSuccess() {
        val session = TwitterCore.getInstance().sessionManager.activeSession
        val authToken = session.authToken
        val token = authToken.token
        //val secret = authToken.secret
        if (token != null) {
            socialAuthentication(token, "twitter")
        }else {
            showMsg(this, "Twitter login failed!", Toast.LENGTH_SHORT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        mTwitterAuthClient.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result: GoogleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                loader.show()
                try {
                    val account = result.getSignInAccount()
                    if (account != null) {
                        val client = OkHttpClient()
                        val requestBody = FormEncodingBuilder()
                            .add("grant_type", "authorization_code")
                            .add(
                                "client_id",
                                resources.getString(R.string.web_application_client_id)
                            )
                            .add("client_secret", resources.getString(R.string.web_application_client_secret))
                            .add("redirect_uri", "")
                            .add("code", account.serverAuthCode)
                            .build();
                        val request = Request.Builder()
                            .url("https://www.googleapis.com/oauth2/v4/token")
                            .post(requestBody)
                            .build();
                        client.newCall(request).enqueue(object :Callback {

                            override fun onFailure(request: Request, e: IOException) {
                                (applicationContext as SignInSignUpActivity).loader.dismiss()
                                showMsg(applicationContext, "Something went wrong, please try again later", Toast.LENGTH_LONG)
                            }

                            override fun onResponse(response: Response) {
                            try {
                                val jsonObject = JSONObject(response.body().string());
                                val access_token = jsonObject.getString("access_token")
                                firebaseAuthWithGoogle(access_token)
                            } catch (e: JSONException) {
                                (applicationContext as SignInSignUpActivity).loader.dismiss()
                                e.printStackTrace();
                            }
                        }
                        });
                    }
                } catch (e: ApiException) {
                    loader.dismiss()
                    showMsg(this, "Google sign in failed", Toast.LENGTH_SHORT)
                }
            }
        }
    }


    private fun firebaseAuthWithGoogle(acct: String) {
        /*val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        val token = acct?.idToken*/

            if (acct != null) {
                acct?.let { it1 -> socialAuthentication(it1, "google-oauth2") }
            } else {
                showMsg(this, "Google sign in failed", Toast.LENGTH_SHORT)
            }
    }

    fun socialAuthentication(accessToken: String,
                provider: String){
        socialLogin(accessToken,provider, object :LoginResponse{

            override fun displayError(message: String) {
                loader.dismiss()
                runOnUiThread(Runnable {
                    applicationContext?.let { showMsg(it, message, Toast.LENGTH_SHORT) }
                })
            }

            override fun success() {
                loader.dismiss()
                startActivity(Intent(applicationContext, MainActivity::class.java).apply {
                    putExtra("localTime", "")
                })
            }

            override fun fail(e:String) {
                loader.dismiss()
                runOnUiThread(Runnable {
                    applicationContext?.let { showMsg(it, e, Toast.LENGTH_SHORT) }
                })

            }
        })

    }

    fun signUpUser(
        phoneNumber: String,
        password: String,
        userName: String,
        location: String,
        emailId: String,
        countryName: String,
        iRegister: IRegister) {
        loader.show()
        if (isOnline(getApplication())) {
            mPreLoginViewModel.registerUser(phoneNumber, password, userName, location, emailId, countryName, iRegister)

        }
        else{
            val snack = Snackbar.make(root_layout,"Please check Internet Connection", Snackbar.LENGTH_LONG)
            snack.view.setBackgroundResource(R.color.colorPrimary);
            snack.show()
            return
        }
    }
    fun sendUserCredentialToServer(emailId: String, password: String, loginResponse: LoginResponse) {
        if (isOnline(getApplication())) {
        loader.show()
        mPreLoginViewModel.logIn(emailId, password, loginResponse,this)
        }
        else{
            val snack = Snackbar.make(root_layout,"Please check Internet Connection", Snackbar.LENGTH_LONG)
            snack.view.setBackgroundResource(R.color.colorPrimary);
            snack.show()
            return
        }
    }
    fun generateOtp(
        phoneNumber: String,
        countryName: String,
        isResend: Boolean,
        isForgot: Boolean,
        iRegister: IRegister) {
        loader.show()
        if (isOnline(getApplication())) {
            mPreLoginViewModel.genrateOtp(phoneNumber, countryName,isResend,isForgot,iRegister)

        }
        else{
            val snack = Snackbar.make(root_layout,"Please check Internet Connection", Snackbar.LENGTH_LONG)
            snack.view.setBackgroundResource(R.color.colorPrimary);
            snack.show()
            return
        }
    }

    fun verifyOtp(
        phoneNumber: String,
        otp: String,
        isForgot: Boolean,
        iRegister: IRegister) {
        loader.show()
        if (isOnline(getApplication())) {
            mPreLoginViewModel.verifyOtp(phoneNumber, otp,isForgot,iRegister)

        }
        else{
            val snack = Snackbar.make(root_layout,"Please check Internet Connection", Snackbar.LENGTH_LONG)
            snack.view.setBackgroundResource(R.color.colorPrimary);
            snack.show()
            return
        }
    }

    fun socialLogin(
        accessToken: String,
        provider: String,
        loginResponse: LoginResponse) {
        //loader.show()
        if (isOnline(getApplication())) {
            mPreLoginViewModel.socialLogin(accessToken, provider,loginResponse, this)

        }
        else{
            val snack = Snackbar.make(root_layout,"Please check Internet Connection", Snackbar.LENGTH_LONG)
            snack.view.setBackgroundResource(R.color.colorPrimary);
            snack.show()
            return
        }
    }

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        showMsg(this, "Please click back again to exit", Toast.LENGTH_SHORT)

        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }
}
