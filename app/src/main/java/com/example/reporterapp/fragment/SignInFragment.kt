package com.example.reporterapp.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.reporterapp.R
import com.example.reporterapp.TYPE
import com.example.reporterapp.Validator
import com.example.reporterapp.activity.ForgotUserIdPasswordActivity
import com.example.reporterapp.activity.MainActivity
import com.example.reporterapp.activity.SignInSignUpActivity
import com.example.reporterapp.callback.LoginResponse
import com.example.reporterapp.showMsg

/* This fragment renders on signIn tab in after the application is launched.
* This class creates an UI for the user to sigIn(If there are login credentials) in to the reporter app*/
class SignInFragment : Fragment(), View.OnClickListener{

    lateinit var userId: EditText
    lateinit var password: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_signin, container, false)

        userId=view.findViewById(R.id.UserId)
        password=view.findViewById(R.id.etPassword)
        view.findViewById<TextView>(R.id.tvSIgnIn).setOnClickListener(this)
        view.findViewById<TextView>(R.id.tvForgotUserId).setOnClickListener(this)
        view.findViewById<TextView>(R.id.tvForgotPassword).setOnClickListener(this)
        return view
    }

    /*This method is called when user wants to signIn to the application, forgot userId, forgot Password*/
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvSIgnIn -> {
                val userId = view?.findViewById<EditText>(R.id.UserId)?.text.toString()
                val password = view?.findViewById<EditText>(R.id.etPassword)?.text.toString()
                if (!Validator.isNotNullNotEmpty(userId)) {
                    context?.let { showMsg(it, "Please enter the Email Id / Mobile number!", Toast.LENGTH_SHORT) }
                    return
                }
                if (!Validator.isNotNullNotEmpty(password)) {
                    context?.let { showMsg(it, "Please enter the password!", Toast.LENGTH_SHORT) }
                    return
                }

                //  (activity as SignInSignUpActivity).sendUserCredentialToServer(userId, password)

                sendUserCredentialsToActivity(object :LoginResponse{

                    override fun displayError(message: String) {

                        (activity as SignInSignUpActivity).loader.dismiss()
                        (activity as SignInSignUpActivity).runOnUiThread(Runnable {
                            context?.let { showMsg(it, message, Toast.LENGTH_SHORT) }
                        })                        }

                    override fun success() {
                        (activity as SignInSignUpActivity).loader.dismiss()
                        (activity as SignInSignUpActivity).finish()
                        startActivity(Intent(context, MainActivity::class.java).apply {
                            putExtra("localTime", "")
                        })
                    }

                    override fun fail(e:String) {
                        (activity as SignInSignUpActivity).loader.dismiss()

                        (activity as SignInSignUpActivity).runOnUiThread(Runnable {
                            context?.let { showMsg(it, e, Toast.LENGTH_SHORT) }
                        })
                        //Toast.makeText(context,e,Toast.LENGTH_SHORT).show()

                    }
                })

            }
            R.id.tvForgotUserId ->
                startActivity(Intent(context, ForgotUserIdPasswordActivity::class.java).apply {
                    putExtra(TYPE, 0)
                })
            R.id.tvForgotPassword ->
                startActivity(Intent(context, ForgotUserIdPasswordActivity::class.java).apply {
                    putExtra(TYPE, 1)
                })
        }
    }


    private fun sendUserCredentialsToActivity(loginResponse: LoginResponse) {
        val emailId = userId.text.toString()
        val userPassword = password.text.toString()

        (activity as SignInSignUpActivity).sendUserCredentialToServer(emailId, userPassword,loginResponse)

    }

}