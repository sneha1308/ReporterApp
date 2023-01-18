package com.example.reporterapp.fragment

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.reporterapp.*
import com.example.reporterapp.activity.SignInSignUpActivity
import com.example.reporterapp.callback.IRegister
import com.example.reporterapp.viewmodel.PreLoginViewModel
import com.hbb20.CountryCodePicker
import kotlinx.android.synthetic.main.fragment_signup.*


/* This fragment renders on signUp tab in after the application is launched.
* This class creates an UI for the user to sign up(If there are no login credentials) in to the reporter app*/
class SignUpFragment : Fragment() {

    private lateinit var mPreLoginViewModel: PreLoginViewModel

    var currentScreen = 0

    /*
    * I need to add "verify" in list string when OTP Authentication is done*/
    //var buttonText = listOf("NEXT", "VERIFY", "SUBMIT")
    val text = "Thank you for submitting your information, we will get in touch with you shortly."

    lateinit var mobileNumber: EditText
    lateinit var createPassword: EditText
    lateinit var confirmPassword: EditText
    lateinit var ccp: CountryCodePicker

    lateinit var enterOtpOne: EditText
    lateinit var editTextTwo: EditText
    lateinit var editTextThree: EditText
    lateinit var editTextFour: EditText

    lateinit var enterName: EditText
    lateinit var enterEmail: EditText
    lateinit var enterLocation: EditText

    var password = ""
    var phoneNumber = ""
    var countryName = ""

    /*This method inflates the UI on the signUp tab*/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_signup, container, false)

/*
        view.findViewById<LinearLayout>(R.id.llRegistrationOtp).visibility = View.GONE
        view.findViewById<LinearLayout>(R.id.llSignUp).visibility = View.VISIBLE
        view.findViewById<TextView>(R.id.tvOtpSignUp).visibility = View.GONE
        view.findViewById<LinearLayout>(R.id.llReporterDetails).visibility = View.GONE
        view.findViewById<LinearLayout>(R.id.verifyButton).visibility = View.GONE
        view.findViewById<LinearLayout>(R.id.nextButton).visibility = View.VISIBLE
        view.findViewById<LinearLayout>(R.id.submitButton).visibility = View.GONE*/

        mobileNumber = view.findViewById(R.id.etEnterMobileNo)
        createPassword = view.findViewById(R.id.etCreatePassword)
        confirmPassword = view.findViewById(R.id.etConfirmPassword)
        ccp = view.findViewById(R.id.ccp)

        enterOtpOne = view.findViewById(R.id.editTextOne)
        editTextTwo = view.findViewById(R.id.editTextTwo)
        editTextThree = view.findViewById(R.id.editTextThree)
        editTextFour = view.findViewById(R.id.editTextFour)

        OtpCursorMoves(enterOtpOne,editTextTwo)
        OtpCursorMoves(editTextTwo,editTextThree)
        OtpCursorMoves(editTextThree,editTextFour)

        enterName = view.findViewById(R.id.etEnterName)
        enterEmail = view.findViewById(R.id.etEnterEmailId)
        enterLocation = view.findViewById(R.id.etEnterLocation)


        view.findViewById<TextView>(R.id.tvRegistrationNext).setOnClickListener {
            if (fetchNumberAndPassword()) {
                sendOtp(false)
            }

        }

        view.findViewById<TextView>(R.id.tvResendOtp).setOnClickListener {
            sendOtp(true)
        }
        view.findViewById<TextView>(R.id.tvRegistrationVerify).setOnClickListener {
            if (fetchOTPDigit()) {
                verifyOtp(object : IRegister {
                    override fun onRegisterSuccess() {
                        (activity as SignInSignUpActivity).loader.dismiss()
                        (activity as SignInSignUpActivity).runOnUiThread(Runnable {
                            context?.let { showMsg(it, "OTP Verified Successfully!", Toast.LENGTH_SHORT) }
                            llRegistrationOtp.visibility = View.GONE
                            tvOtpSignUp.visibility = View.GONE
                            llReporterDetails.visibility = View.VISIBLE
                            verifyButton.visibility = View.GONE
                            nextButton.visibility = View.GONE
                            submitButton.visibility = View.VISIBLE
                            enterName.text.clear()
                            enterEmail.text.clear()
                            enterLocation.text.clear()
                        })
                    }

                    override fun onRegisterFailure(message: String) {
                        (activity as SignInSignUpActivity).loader.dismiss()
                        (activity as SignInSignUpActivity).runOnUiThread(Runnable {
                            context?.let { showMsg(it, message, Toast.LENGTH_SHORT) }
                        })
                        return
                    }

                    override fun displayError(message: String) {
                        (activity as SignInSignUpActivity).loader.dismiss()
                        (activity as SignInSignUpActivity).runOnUiThread(Runnable {
                            context?.let { showMsg(it, message, Toast.LENGTH_SHORT) }
                        })
                        return
                    }
                })


            }

        }

        view.findViewById<TextView>(R.id.tvRegistrationSubmit).setOnClickListener {
            createUser(object : IRegister {
                override fun onRegisterSuccess() {
                    (activity as SignInSignUpActivity).loader.dismiss()
                    (activity as SignInSignUpActivity).runOnUiThread(Runnable {
                        getCustomDialog(R.layout.custom_dialog_box, text, activity as Activity).show()
                    })
                }

                override fun onRegisterFailure(message: String) {
                    (activity as SignInSignUpActivity).loader.dismiss()
                    (activity as SignInSignUpActivity).runOnUiThread(Runnable {
                        context?.let { showMsg(it, message, Toast.LENGTH_SHORT) }
                    })
                }

                override fun displayError(message: String) {
                    (activity as SignInSignUpActivity).loader.dismiss()
                    (activity as SignInSignUpActivity).runOnUiThread(Runnable {
                        context?.let { showMsg(it, message, Toast.LENGTH_SHORT) }
                    })
                }
            })
        }

        /*   getView()?.isFocusableInTouchMode = true
           getView()?.requestFocus()
           getView()?.setOnKeyListener(object : View.OnKeyListener {
               override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
                   return if (keyCode == KeyEvent.KEYCODE_BACK) {
                       Toast.makeText(context,"BACK Button Pressed",Toast.LENGTH_SHORT).show()

                       true
                   } else false
               }
           })*/

        return view
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            fragmentManager!!.beginTransaction().detach(this).attach(this).commit()
        }
    }

   fun sendOtp(isResend: Boolean){
       generateOtp(isResend,object : IRegister {
           override fun onRegisterSuccess() {
               (activity as SignInSignUpActivity).loader.dismiss()
               (activity as SignInSignUpActivity).runOnUiThread(Runnable {
                   if(!isResend) {
                       context?.let { showMsg(it, "OTP has been sent Successfully!", Toast.LENGTH_SHORT) }
                       llSignUp.visibility = View.GONE
                       relativeLayout.visibility = View.VISIBLE
                       llReporterDetails.visibility = View.GONE
                       verifyButton.visibility = View.VISIBLE
                       nextButton.visibility = View.GONE
                       submitButton.visibility = View.GONE

                   }
                   else{
                       context?.let { showMsg(it, "OTP has been resent successfully!", Toast.LENGTH_SHORT) }
                   }
               })
           }

           override fun onRegisterFailure(message: String) {
               (activity as SignInSignUpActivity).loader.dismiss()
               (activity as SignInSignUpActivity).runOnUiThread(Runnable {
                   context?.let { showMsg(it, message, Toast.LENGTH_SHORT) }
               })
               return
           }

           override fun displayError(message: String) {
               (activity as SignInSignUpActivity).loader.dismiss()
               (activity as SignInSignUpActivity).runOnUiThread(Runnable {
                   context?.let { showMsg(it, message, Toast.LENGTH_SHORT) }
               })
               return
           }
       })
       enterOtpOne.setFocusableInTouchMode(true);
       enterOtpOne.requestFocus();
       showKeyboard(activity as SignInSignUpActivity)

   }

    private fun fetchNumberAndPassword(): Boolean {
        phoneNumber = mobileNumber.text.toString()
        val createPassword = createPassword.text.toString()
        val confirmPassword = confirmPassword.text.toString()
        val countryCode = ccp.selectedCountryCodeWithPlus
        val countryCodeName = ccp.selectedCountryNameCode

        if (!Validator.isNotNullNotEmpty(phoneNumber) || !Validator.isNotNullNotEmpty(createPassword)) {
            context?.let { it1 -> showMsg(it1, "Please enter mobile number & password", Toast.LENGTH_SHORT) }
            return false
        }

        if (phoneNumber.length > 10 || phoneNumber.length < 8) {
            context?.let { showMsg(it, "Please enter valid mobile Number", Toast.LENGTH_SHORT) }
            return false
        }
        if (countryCode == null || countryCode.isEmpty()) {
            context?.let { showMsg(it, "Please select the Country code", Toast.LENGTH_SHORT) }
            return false
        }
        if (createPassword != confirmPassword) {
            context?.let { showMsg(it, "Confirm Password mismatch with the new password", Toast.LENGTH_SHORT) }
            return false
        }
        if (!Validator.isValidPassword(createPassword)) {
            context?.let {
                showMsg(
                    it,
                    "Try using a minimum of 8 characters and a mixture of UPPERCASE, lowercase, digits(0-9) & special characters",
                    Toast.LENGTH_LONG
                )
            }
            return false
        }
        if (!Patterns.PHONE.matcher(phoneNumber).matches()) {
            context?.let { showMsg(it, "Incorrect mobile number", Toast.LENGTH_SHORT) }
            return false
        }

        phoneNumber = phoneNumber
        password = createPassword
        countryName = countryCode

        return true
    }

    fun fetchOTPFromServer() {
        //phoneNumber
        //httpsdsdsd. muter(number)
    }

    fun fetchOTPDigit(): Boolean {
        val firstDigit = enterOtpOne.text.toString()
        val secondDigit = editTextTwo.text.toString()
        val ThirdDigit = editTextThree.text.toString()
        val fourthDigit = editTextFour.text.toString()

        if (firstDigit.isEmpty() || secondDigit.isEmpty() || ThirdDigit.isEmpty() || fourthDigit.isEmpty()) {
            context?.let { showMsg(it, "Please enter OTP!", Toast.LENGTH_SHORT) }
            return false
        }

        // sendOTPToServer((firstDigit + secondDigit + ThirdDigit + fourthDigit).toInt())
        return true
    }

    /*fun sendOTPToServer(value: Int) {

    }*/

    private fun createUser(iRegister: IRegister) {
        val createPassword = createPassword.text.toString()
        val confirmPassword = confirmPassword.text.toString()

        if (createPassword == confirmPassword)
            password = createPassword

        val getUserName = enterName.text.toString()
        val getEmail = enterEmail.text.toString()
        val getLocation = enterLocation.text.toString()

        if (TextUtils.isEmpty(getUserName)) {
            context?.let { showMsg(it, "Please enter Username", Toast.LENGTH_SHORT) }
            return
        }
        if (TextUtils.isEmpty(getEmail)) {
            context?.let { showMsg(it, "Please enter Email Id", Toast.LENGTH_SHORT) }
            return
        }
        if (TextUtils.isEmpty(getLocation)) {
            context?.let { showMsg(it, "Please enter Location", Toast.LENGTH_SHORT) }
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(getEmail).matches()) {
            context?.let { showMsg(it, "Please enter valid Email Id", Toast.LENGTH_SHORT) }
            return
        }

        (activity as SignInSignUpActivity).signUpUser(
            phoneNumber,
            password,
            getUserName,
            getLocation,
            getEmail,
            countryName,
            iRegister
        )

    }

    private fun generateOtp(isResend: Boolean, iRegister: IRegister) {

        if (editTextFour.text.isNotEmpty() || editTextThree.text.isNotEmpty() || editTextTwo.text.isNotEmpty() || enterOtpOne.text.isNotEmpty()) {
            editTextFour.text.clear()
            editTextThree.text.clear()
            editTextTwo.text.clear()
            enterOtpOne.text.clear()
        }

        (activity as SignInSignUpActivity).generateOtp(
            phoneNumber,
            countryName,
            isResend,
            false,
            iRegister
        )

    }
    private fun verifyOtp(iRegister: IRegister) {
        val firstDigit = enterOtpOne.text.toString()
        val secondDigit = editTextTwo.text.toString()
        val ThirdDigit = editTextThree.text.toString()
        val fourthDigit = editTextFour.text.toString()
        (activity as SignInSignUpActivity).verifyOtp(
            phoneNumber,
            firstDigit+secondDigit+ThirdDigit+fourthDigit,
            false,
            iRegister
        )

    }
}