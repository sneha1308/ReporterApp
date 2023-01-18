package com.example.reporterapp.fragment

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.reporterapp.R
import com.example.reporterapp.Validator
import com.example.reporterapp.activity.ForgotUserIdPasswordActivity
import com.example.reporterapp.callback.IRegister
import com.example.reporterapp.callback.ResultCallback
import com.example.reporterapp.isOnline
import com.example.reporterapp.showMsg
import com.example.reporterapp.viewmodel.PreLoginViewModel
import kotlinx.android.synthetic.main.activity_signin_signup.*



@SuppressLint("ValidFragment")
class FragmentUpdatePassword() : Fragment() {
    lateinit var resultCallback: ResultCallback
    lateinit var paswordEditText: EditText
    lateinit var confirmPasswordEditText: EditText
    private lateinit var mPreLoginViewModel: PreLoginViewModel


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_update_password, container, false)
        mPreLoginViewModel = ViewModelProviders.of(this).get(PreLoginViewModel::class.java)
        val arguments = arguments
        val mobile = arguments?.getString("mobile")
        paswordEditText = view.findViewById(R.id.etEnterNewPassword)
        confirmPasswordEditText = view.findViewById(R.id.etEnterConfirmPassword)
        view.findViewById<TextView>(R.id.tvSubmitNewPassword).setOnClickListener {
            val password = paswordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            if(password.isEmpty()){
                context?.let { showMsg(it, "Please Enter Password", Toast.LENGTH_SHORT) }
            }
            else if(confirmPassword.isEmpty()){
                context?.let { showMsg(it, "Please Enter Confirm Password", Toast.LENGTH_SHORT) }
            }
            else if(password != confirmPassword){
                context?.let { showMsg(it, "Confirm Password mismatch with the new password", Toast.LENGTH_SHORT) }
            }
            else if(!Validator.isValidPassword(password)){
                context?.let {
                    showMsg(
                        it,
                        "Try using a minimum of 8 characters and a mixture of UPPERCASE, lowercase, digits(0-9) & special characters",
                        Toast.LENGTH_LONG
                    )
                }
            }
            else if(mobile == null || mobile.isEmpty()){
                context?.let { showMsg(it, "Something went wrong!", Toast.LENGTH_SHORT) }
            }
            else {
                resetPassword(mobile, password)
            }
        }

        return view
    }

    private fun resetPassword(mobile:String, password: String) {
        resetPassword(mobile, password,object : IRegister {
            override fun onRegisterSuccess() {
                (activity as ForgotUserIdPasswordActivity).loader.dismiss()
                (activity as ForgotUserIdPasswordActivity).runOnUiThread(Runnable {
                    //context?.let { showMsg(it, "Your new password has been successfully updated!", Toast.LENGTH_SHORT) }
                    resultCallback.result(true,"")
                })
            }

            override fun onRegisterFailure(message: String) {
                (activity as ForgotUserIdPasswordActivity).loader.dismiss()
                (activity as ForgotUserIdPasswordActivity).runOnUiThread(Runnable {
                    context?.let { showMsg(it, message, Toast.LENGTH_SHORT) }
                })
                return
            }

            override fun displayError(message: String) {
                (activity as ForgotUserIdPasswordActivity).loader.dismiss()
                (activity as ForgotUserIdPasswordActivity).runOnUiThread(Runnable {
                    context?.let { showMsg(it, message, Toast.LENGTH_SHORT) }
                })
                return
            }
        })
    }

    fun resetPassword(mobile: String, password: String, iRegister: IRegister) {
        if (context?.let { isOnline(it) }!!) {
            (activity as ForgotUserIdPasswordActivity).loader.show()
            mPreLoginViewModel.resetPassword(mobile, password, iRegister)
        }
        else{
            val snack = Snackbar.make(root_layout,"Please check Internet Connection", Snackbar.LENGTH_LONG)
            snack.view.setBackgroundResource(R.color.colorPrimary);
            snack.show()
            return
        }
    }
}