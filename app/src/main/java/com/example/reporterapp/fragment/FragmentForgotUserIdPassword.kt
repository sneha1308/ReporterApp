package com.example.reporterapp.fragment

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
import com.example.reporterapp.*
import com.example.reporterapp.activity.ForgotUserIdPasswordActivity
import com.example.reporterapp.activity.SignInSignUpActivity
import com.example.reporterapp.callback.IRegister
import com.example.reporterapp.callback.ResultCallback
import com.example.reporterapp.viewmodel.PreLoginViewModel
import com.hbb20.CountryCodePicker
import kotlinx.android.synthetic.main.activity_signin_signup.*
import kotlinx.android.synthetic.main.fragment_forgot_userid_password.*

class FragmentForgotUserIdPassword : Fragment() {
    lateinit var resultCallback: ResultCallback

    private lateinit var mPreLoginViewModel: PreLoginViewModel

    lateinit var ccp: CountryCodePicker
    var phoneNumber = ""
    var countryName = ""
    lateinit var enterOtpOne: EditText
    lateinit var editTextTwo: EditText
    lateinit var editTextThree: EditText
    lateinit var editTextFour: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_forgot_userid_password, container, false)
        mPreLoginViewModel = ViewModelProviders.of(this).get(PreLoginViewModel::class.java)
       val mobileEditText = view.findViewById<EditText>(R.id.etEnterRegisteredMobileNo)
       val ccp = view.findViewById<CountryCodePicker>(R.id.ccp)

        enterOtpOne = view.findViewById(R.id.editTextOne)
        editTextTwo = view.findViewById(R.id.editTextTwo)
        editTextThree = view.findViewById(R.id.editTextThree)
        editTextFour = view.findViewById(R.id.editTextFour)

        OtpCursorMoves(enterOtpOne,editTextTwo)
        OtpCursorMoves(editTextTwo,editTextThree)
        OtpCursorMoves(editTextThree,editTextFour)

        view.findViewById<TextView>(R.id.tvSubmitNumber).setOnClickListener {
            phoneNumber = mobileEditText.text.toString()
            val countryCode = ccp.selectedCountryCodeWithPlus
            val countryCodeName = ccp.selectedCountryNameCode
            if (phoneNumber.isEmpty()) {
                context?.let { showMsg(it, "Please Enter Mobile Number", Toast.LENGTH_SHORT) }
            } else if (phoneNumber.length > 10 || phoneNumber.length < 8) {
                context?.let { showMsg(it, "Please Enter valid Phone Number", Toast.LENGTH_SHORT) }
            } else if (countryCode == null || countryCode.isEmpty()) {
                context?.let { showMsg(it, "Please select the Country code", Toast.LENGTH_SHORT) }
            } else {
                phoneNumber =  phoneNumber
                countryName = countryCode
                sendOtp(false)
            }
        }
        view.findViewById<TextView>(R.id.tvResendOtp).setOnClickListener {
            phoneNumber = mobileEditText.text.toString()
            val countryCode = ccp.selectedCountryCodeWithPlus
            val countryCodeName = ccp.selectedCountryNameCode
            if (phoneNumber.isEmpty()) {
                context?.let { showMsg(it, "Please Enter Mobile Number", Toast.LENGTH_SHORT) }
            } else if (phoneNumber.length > 10 || phoneNumber.length < 8) {
                context?.let { showMsg(it, "Please Enter valid Phone Number", Toast.LENGTH_SHORT) }
            } else if (countryCode == null || countryCode.isEmpty()) {
                context?.let { showMsg(it, "Please select the Country code", Toast.LENGTH_SHORT) }
            } else {
                phoneNumber =  phoneNumber
                countryName = countryCode
                sendOtp(true)
            }
        }
        view.findViewById<TextView>(R.id.tvVerifyNumber).setOnClickListener {
            if (fetchOTPDigit()) {
                verifyOtp(object : IRegister {
                    override fun onRegisterSuccess() {
                        (activity as ForgotUserIdPasswordActivity).loader.dismiss()
                        (activity as ForgotUserIdPasswordActivity).runOnUiThread(Runnable {
                            context?.let { showMsg(it, "OTP Verified Successfully!", Toast.LENGTH_SHORT) }

                            resultCallback.result(false,phoneNumber)
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
            else {
                return@setOnClickListener
            }
        }
        return view
    }

    fun sendOtp(isResed: Boolean){
        generateOtp(isResed,object : IRegister {
            override fun onRegisterSuccess() {
                (activity as ForgotUserIdPasswordActivity).loader.dismiss()
                (activity as ForgotUserIdPasswordActivity).runOnUiThread(Runnable {
                    if(isResed)
                        context?.let { showMsg(it, "OTP has been resent successfully!", Toast.LENGTH_SHORT) }
                    else
                        context?.let { showMsg(it, "OTP has been sent Successfully!", Toast.LENGTH_SHORT) }
                    tvTextOtp.visibility = View.VISIBLE
                    llEnterOtp.visibility = View.VISIBLE
                    tvSubmitNumber.visibility = View.GONE
                    tvVerifyNumber.visibility = View.VISIBLE
                    enterOtpOne.setFocusableInTouchMode(true);
                    enterOtpOne.requestFocus();
                    showKeyboard(activity as ForgotUserIdPasswordActivity)
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

    fun generateOtp(isResed: Boolean, iRegister: IRegister) {
        if (context?.let { isOnline(it) }!!) {
            if (isResed){
                if (editTextFour.text.isNotEmpty() || editTextThree.text.isNotEmpty() || editTextTwo.text.isNotEmpty() || enterOtpOne.text.isNotEmpty()) {
                    editTextFour.text.clear()
                    editTextThree.text.clear()
                    editTextTwo.text.clear()
                    enterOtpOne.text.clear()
                }
            }
            (activity as ForgotUserIdPasswordActivity).loader.show()
            mPreLoginViewModel.genrateOtp(phoneNumber, countryName, isResed, true, iRegister)
        }
        else{
            val snack = Snackbar.make(root_layout,"Please check Internet Connection", Snackbar.LENGTH_LONG)
            snack.view.setBackgroundResource(R.color.colorPrimary);
            snack.show()
            return
        }
    }

    fun verifyOtp(iRegister: IRegister) {
        val firstDigit = enterOtpOne.text.toString()
        val secondDigit = editTextTwo.text.toString()
        val ThirdDigit = editTextThree.text.toString()
        val fourthDigit = editTextFour.text.toString()
        if (context?.let { isOnline(it) }!!) {
            (activity as ForgotUserIdPasswordActivity).loader.show()
            mPreLoginViewModel.verifyOtp(
                phoneNumber,
                firstDigit + secondDigit + ThirdDigit + fourthDigit,
                true,
                iRegister
            )
        } else {
            val snack = Snackbar.make(root_layout, "Please check Internet Connection", Snackbar.LENGTH_LONG)
            snack.view.setBackgroundResource(R.color.colorPrimary);
            snack.show()
            return
        }
    }

    fun fetchOTPDigit(): Boolean {
        val firstDigit = enterOtpOne.text.toString()
        val secondDigit = editTextTwo.text.toString()
        val ThirdDigit = editTextThree.text.toString()
        val fourthDigit = editTextFour.text.toString()

        if (firstDigit.isEmpty() || secondDigit.isEmpty() || ThirdDigit.isEmpty() || fourthDigit.isEmpty()) {
            context?.let { showMsg(it, "please Enter otp!", Toast.LENGTH_SHORT) }
            return false
        }

        // sendOTPToServer((firstDigit + secondDigit + ThirdDigit + fourthDigit).toInt())
        return true
    }
}

