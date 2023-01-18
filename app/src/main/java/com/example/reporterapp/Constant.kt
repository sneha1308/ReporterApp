package com.example.reporterapp

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.PorterDuff
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Environment
import android.support.design.widget.Snackbar
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.reporterapp.activity.SignInSignUpActivity
import java.io.File
import java.net.URISyntaxException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

const val ARTICLE_ID = "article_id"
const val IS_EDIT = "is_editable"
const val TYPE = "type"
const val NOTIFICATION_STATUS = "notification_status"

fun isAlphaNumeric(s: String): Boolean {
    val pattern = "^[a-zA-Z0-9- ]*$"
    return s.matches(pattern.toRegex())
}

fun isNotNullNotEmpty(text: String): Boolean {
    return (text.replace(" ", "").isNotEmpty() && "null" != text)
}

fun isValidPassword(password: String): Boolean {
    val pattern: Pattern
    val matcher: Matcher
    val passwordPattern = "((?=\\S+$).{8,})"
    pattern = Pattern.compile(passwordPattern)
    matcher = pattern.matcher(password)
    return matcher.matches()
}

enum class ForgotType {
    UserID, Password, NONE
}

/*enum class Language {
    ENGLISH, TELUGU
}*/

enum class ArticleStatus(val caption: String) {
    DRAFT("DRAFTS"),
    RE_WORK("RE-WORK"),
    PENDING("PENDING"),
    PUBLISHED("PUBLISHED"),
    REJECTED("REJECTED"),
    NONE("NONE")
}

fun getDateString(sec: Long): String {
    val sdf = SimpleDateFormat("MMM dd,yyyy")
    val resultDate = Date(sec)
    return sdf.format(resultDate)
}

fun getLastLoginString(sec: Long): String {
    val sdf = SimpleDateFormat("MMM dd yyyy,HH:mm")
    val resultDate = Date(sec)
    return sdf.format(resultDate)
}

fun getFileName(completeName: String): String {

    val xx = completeName.split("/")
    if (xx.isNotEmpty()) {
        return xx.last()
    }
    return "No File Name Found."
}

fun customDialog(layout: Int, context: Context): Dialog {
    val dialog = Dialog(context)
    dialog.setCanceledOnTouchOutside(false)
    dialog.setContentView(layout)
    val lp = WindowManager.LayoutParams()
    val window = dialog.window
    lp.copyFrom(window!!.attributes)
    // This makes the dialog take up the full width
    lp.width = WindowManager.LayoutParams.MATCH_PARENT
    lp.height = WindowManager.LayoutParams.WRAP_CONTENT
    window.attributes = lp
    return dialog
}

fun audioCustomDialog(layout: Int, context: Context): Dialog {
    val dialog = Dialog(context)
    dialog.setCanceledOnTouchOutside(false)
    dialog.setContentView(layout)
    val lp = WindowManager.LayoutParams()
    val window = dialog.window
    lp.copyFrom(window!!.attributes)
    // This makes the dialog take up the full width
    lp.width = WindowManager.LayoutParams.MATCH_PARENT
    lp.height = WindowManager.LayoutParams.MATCH_PARENT
    window.attributes = lp
    return dialog
}

fun getCustomDialog(layout: Int, dialogMessage: String, context: Activity): Dialog {
    val dialog = customDialog(layout, context)
    (dialog.findViewById(R.id.tvDialogMessage) as TextView).text = dialogMessage
    (dialog.findViewById(R.id.tvBackToSignIn) as TextView).setOnClickListener {
        dialog.dismiss()
        context.startActivity(Intent(context, SignInSignUpActivity::class.java))
    }
    return dialog
}

/*fun saveAccount(layout: Int,context: Context):Dialog{
    val dialog= customDialog(layout,context)
    (dialog.findViewById(R.id.tvSavePassword) as TextView).setOnClickListener {
        dialog.dismiss()
    }
    (dialog.findViewById(R.id.tvCancelDialog) as TextView).setOnClickListener {
        dialog.dismiss()
    }
    return dialog
}*/

@Throws(URISyntaxException::class)
fun getPath(context: Context, uri: Uri): String? {
    if ("content".equals(uri.scheme, ignoreCase = true)) {
        val projection = arrayOf("_data")
        val cursor: Cursor?

        try {
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            val column_index = cursor!!.getColumnIndexOrThrow("_data")
            if (cursor.moveToFirst()) {
                return cursor.getString(column_index)
            }
        } catch (e: Exception) {
            // Eat it
        }

    } else if ("file".equals(uri.scheme, ignoreCase = true)) {
        return uri.path
    }

    return null
}

val RECORDING_PATH = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)}"

fun createRecordingFolder(): Boolean {
    return if (!File(RECORDING_PATH).exists()) {
        File(RECORDING_PATH).mkdir()
    } else {
        true
    }
}

fun getAudioFileName():String{
    createRecordingFolder()
    val simpleDateFormat = SimpleDateFormat("dd-mm-yyyy-hh-mm-ss")
    val date = simpleDateFormat.format(Date(System.currentTimeMillis()))
    return "$RECORDING_PATH/recording_$date.mp3"
}

fun isOnline(context:Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.activeNetworkInfo != null && cm.activeNetworkInfo.isConnected
}

fun getLocalTime():Pair<String,String>{

    val cal=Calendar.getInstance()
    cal.timeZone= TimeZone.getTimeZone("UTC")

    val dateFormat=SimpleDateFormat("yyyy-MM-dd")
    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
    val formattedDate= dateFormat.format(cal.time)

    val timeFormat = SimpleDateFormat("HH:mm:ss")
    timeFormat.timeZone = TimeZone.getTimeZone("UTC")

    val formattedTime = timeFormat.format(cal.time)
    return Pair(formattedDate,formattedTime)
}


    fun dateFormatter(date: String): Long {
        val timeZone: TimeZone = TimeZone.getDefault()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        formatter.timeZone = timeZone

        var convertedDate = formatter.parse(date)
        return convertedDate.time
    }

fun dateAndYearFormatter(date: String): Long {
    val timeZone: TimeZone = TimeZone.getDefault()
    val formatter = SimpleDateFormat("MMM dd,yyyy", Locale.getDefault())
    formatter.timeZone = timeZone

    var convertedDate = formatter.parse(date)
    return convertedDate.time
}

@Throws(ParseException::class)
fun convertToNewFormat(dateStr: String): Long {
    val utc = TimeZone.getTimeZone("UTC")
    val date: String = dateStr.replaceAfter("+","")
    val sourceFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
    val destFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    destFormat.timeZone = TimeZone.getDefault()
    sourceFormat.timeZone = utc
    val convertedDate = sourceFormat.parse(date)
    var convertedLocalDate = destFormat.parse(destFormat.format(convertedDate))
    return convertedLocalDate.time
}

    fun hideKeyboard(activity: Activity) {
    val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view = activity.currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(activity)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun showKeyboard(activity: Activity) {
    val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view = activity.currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(activity)
    }
    imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

fun noNetworkSnackBar(view:View,  onClickListener:View.OnClickListener){
    val snackbar = Snackbar
        .make(view, "No internet connection!", Snackbar.LENGTH_LONG)
        .setAction("RETRY", onClickListener)
    snackbar.show()
}

fun localToUTC(): String {
    val date = Date()
    val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(date)
}

fun localToUTCForArticles(): String {
    val date = Date()
    val sdf = SimpleDateFormat("yyyy/MM/dd HH")
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    return sdf.format(date)
}

fun showMsg(ctx: Context, msg: String, duration: Int) {
    val toast = Toast.makeText(ctx, msg, duration)
    val view = toast.view
    view.background.setColorFilter(ctx.resources.getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN)
    val text = view.findViewById<TextView>(android.R.id.message)
    text.setTextColor(ctx.resources.getColor(R.color.white))
    toast.show()
}

fun OtpCursorMoves(first: EditText, target: EditText) {
    first.addTextChangedListener(object : TextWatcher {

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (first.getText().toString().length === 1) {
                target.requestFocus()
            }
        }

        override fun beforeTextChanged(
            s: CharSequence, start: Int,
            count: Int, after: Int
        ) {

        }

        override fun afterTextChanged(s: Editable) {
        }

    })

    target.addTextChangedListener(object : TextWatcher {

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (target.getText().toString().length === 0) {
                first.requestFocus()
            }
        }

        override fun beforeTextChanged(
            s: CharSequence, start: Int,
            count: Int, after: Int
        ) {

        }

        override fun afterTextChanged(s: Editable) {
        }

    })
}

fun getAlertDialog(activity: Activity): AlertDialog {
    val dialogBuilder = AlertDialog.Builder(activity);
    val inflater = LayoutInflater.from(activity).inflate(R.layout.progress_dialog_layout, null)
    val dialogView:View = inflater
    dialogBuilder.setView(dialogView);
    dialogBuilder.setCancelable(false);
    return dialogBuilder.create();

}

