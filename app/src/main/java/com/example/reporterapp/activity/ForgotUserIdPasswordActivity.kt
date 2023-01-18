package com.example.reporterapp.activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.reporterapp.*
import com.example.reporterapp.callback.ResultCallback
import com.example.reporterapp.fragment.FragmentForgotUserIdPassword
import com.example.reporterapp.fragment.FragmentUpdatePassword
import java.lang.RuntimeException


class ForgotUserIdPasswordActivity : AppCompatActivity(), ResultCallback {

    private var forgotType = ForgotType.NONE
    lateinit var loader: AlertDialog

    var isFragmentLoaded = true
    private val popUpText = listOf(
        "An email has been sent to 'Your Register Mail ID' containing the User ID",
        "             Your password has been successfully changed.               "
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_user_id)

        val toolbar=findViewById<android.support.v7.widget.Toolbar>(R.id.toolbarTransparent)
        setSupportActionBar(toolbar)

        loader = getAlertDialog(this)
        loader.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val progressRunnable = object : Runnable {
            override fun run() {
                if(loader.isShowing) {
                    showMsg(
                        this@ForgotUserIdPasswordActivity,
                        "Timeout! please check your internet connection",
                        Toast.LENGTH_SHORT
                    )
                    loader.dismiss()
                }
            }

        }
        val pdCanceller = Handler();
        pdCanceller.postDelayed(progressRunnable, 20000);

        val type = intent.getIntExtra(TYPE, -1)

        if (type == -1) {
            throw RuntimeException(resources.getString(R.string.type_not_should_be_minus_one_forgot_user_id_activity))
        } else {
            forgotType = ForgotType.values()[type]
        }

        toolbar.findViewById<TextView>(R.id.textView).text="Forgot ${forgotType.name}"
        toolbar.findViewById<ImageView>(R.id.back_button)?.setOnClickListener {
            onBackPressed()
        }

        fragmentForgotUserIdPassword()
    }


    private fun fragmentForgotUserIdPassword() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(
            R.id.fragment_holder,
            FragmentForgotUserIdPassword().apply { resultCallback = this@ForgotUserIdPasswordActivity })
        fragmentTransaction.commit()
    }

    private fun fragmentUpdatePassword(mobile:String) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val fragment:FragmentUpdatePassword = FragmentUpdatePassword()
        val arguments = Bundle()
        arguments.putString("mobile", mobile)
        fragment.setArguments(arguments)
        fragment.apply {
            resultCallback = this@ForgotUserIdPasswordActivity
        }
        fragmentTransaction.replace(R.id.fragment_holder, fragment)
        fragmentTransaction.commit()
    }

    override fun result(isLast: Boolean,mobile:String) {
        when (forgotType) {
            ForgotType.UserID -> {
                showPopupDialog()
            }
            ForgotType.Password -> {
                if (isLast) showPopupDialog() else fragmentUpdatePassword(mobile)
            }
            ForgotType.NONE -> {
                throw RuntimeException("Type not should be $forgotType")
            }
        }
    }

    private fun showPopupDialog() {
        //Toast.makeText(this, popUpText[forgotType.ordinal], Toast.LENGTH_SHORT).show()
        getCustomDialog(R.layout.custom_dialog_box,popUpText[forgotType.ordinal],this).show()
    }
}
