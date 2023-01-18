package com.example.reporterapp

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.view.View

import android.content.Context.MODE_PRIVATE

object PermissionUtil {

    private val PREFS_FILE_NAME = "abc"

    private fun shouldAskPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    private fun shouldAskPermission(context: Context, permission: String): Boolean {
        if (shouldAskPermission()) {
            val permissionResult = ActivityCompat.checkSelfPermission(context, permission)
            if (permissionResult != PackageManager.PERMISSION_GRANTED) {
                return true
            }
        }
        return false
    }

    fun checkPermission(context: Activity, permission: String, listener: PermissionAskListener) {

        if (shouldAskPermission(context, permission)) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                listener.onPermissionPreviouslyDenied()
            } else {

                if (isFirstTimeAskingPermission(context, permission)) {
                    firstTimeAskingPermission(context, permission, false)
                    listener.onNeedPermission()
                } else {
                    listener.onPermissionDisabled()
                }
            }
        } else {
            listener.onPermissionGranted()
        }
    }

    fun checkPermissions(context: Activity, permission: String, listener: PermissionAskListener) {

        if (shouldAskPermission(context, permission)) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                listener.onPermissionPreviouslyDenied()
            } else {

                if (isFirstTimeAskingPermission(context, permission)) {
                    firstTimeAskingPermission(context, permission, false)
                    listener.onNeedPermission()
                } else {
                    listener.onPermissionDisabled()
                }
            }
        } else {
            listener.onPermissionGranted()
        }
    }

    private fun firstTimeAskingPermission(context: Context, permission: String, isFirstTime: Boolean) {
        val sharedPreference = context.getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE)
        sharedPreference.edit().putBoolean(permission, isFirstTime).apply()
    }

    private fun isFirstTimeAskingPermission(context: Context, permission: String): Boolean {
        return context.getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE).getBoolean(permission, true)
    }

    fun showExplanation(
        context: Activity,
        title: String,
        message: String,
        permission: String,
        permissionRequestCode: Int
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok) { dialog, id ->
                requestPermission(
                    context,
                    permission,
                    permissionRequestCode
                )
            }
        builder.create().show()
    }

    private fun requestPermission(context: Activity, permissionName: String, permissionRequestCode: Int) {
        ActivityCompat.requestPermissions(context, arrayOf(permissionName), permissionRequestCode)
    }

    fun showSnackbar(activity: Activity, layout: View) {
        val snackbar = Snackbar.make(layout, "Allow permissions manually", Snackbar.LENGTH_INDEFINITE)
            .setAction("OPEN SETTINGS") { PermissionUtil.openSettings(activity) }
        snackbar.view.setBackgroundResource(R.color.colorPrimary);
        snackbar.show()

    }

    fun openSettings(context: Context) {
        val intent =
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /*
     * Callback on various cases on checking permission
     *
     * 1.  Below M, runtime permission not needed. In that case onPermissionGranted() would be called.
     *     If permission is already granted, onPermissionGranted() would be called.
     *
     * 2.  Above M, if the permission is being asked first time onNeedPermission() would be called.
     *
     * 3.  Above M, if the permission is previously asked but not granted, onPermissionPreviouslyDenied()
     *     would be called.
     *
     * 4.  Above M, if the permission is disabled by device policy or the user checked "Never ask again"
     *     check box on previous request permission, onPermissionDisabled() would be called.
     * */
    interface PermissionAskListener {

        fun onNeedPermission()

        fun onPermissionPreviouslyDenied()

        fun onPermissionDisabled()

        fun onPermissionGranted()
    }
}


/*  @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("MainActivity","permision granted in onRequestPermissionsResult");
                    //readContacts()
                    // permission was granted, yay! do the
                    // calendar task you need to do.
                    Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'switch' lines to check for other
            // permissions this app might request
        }
    }*/


/*public void read(final Activity context){
        PermissionUtil.checkPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                new PermissionUtil.PermissionAskListener() {

                    @Override
                    public void onNeedPermission() {
                        Log.e("MainActivity","permission requested");
                        ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
                    }
                    @Override
                    public void onPermissionPreviouslyDenied() {
                        showExplanation("Permission is required","This is use",Manifest.permission.WRITE_EXTERNAL_STORAGE,REQUEST_EXTERNAL_STORAGE);
                    }
                    @Override
                    public void onPermissionDisabled() {
                        Toast.makeText(context, "Permission Disabled.", Toast.LENGTH_SHORT).show();

                        Snackbar snackbar = Snackbar.make(layout, "Message is deleted", Snackbar.LENGTH_LONG)
                                .setAction("UNDO", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Snackbar snackbar1 = Snackbar.make(layoutCom, "Message is restored!", Snackbar.LENGTH_SHORT);
                                        snackbar1.show();
                                        //openSettings();
                                    }
                                });

                        snackbar.show();

                    }
                    @Override
                    public void onPermissionGranted() {
                        Log.e("MainActivity","permision granted");
                        //Do what you want to do.
                        //readContacts();
                    }
                });
    }*/