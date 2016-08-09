package com.kaist.safetydriving.Util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.kaist.safetydriving.Interface.PermissionActionListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ActivityUtilities {
    private static final String TAG = ActivityUtilities.class.getSimpleName();
    public static final int PERMISSIONS_DEFAULT = -1;
    public static final int PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    public static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;
    public static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 3;
    public static final int PERMISSIONS_REQUEST_READ_SMS = 4;
    public static final int PERMISSIONS_REQUEST_GET_ACCOUNTS = 5;
    public static final int PERMISSIONS_REQUEST_CAMERA_VISUALIZER = 6;
    public static final int PERMISSIONS_REQUEST_CAMERA_PHOTO = 7;
    public static final int PERMISSIONS_REQUEST_CAMERA_VIDEO = 8;
    public static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 9;
    public static final int PERMISSIONS_REQUEST_CALL_PHONE = 10;
    private static boolean isShowDownloadNotification = false;




    public static void onRequestPermissionsResult(Activity activity, int requestCode,
                                             String permissions[], int[] grantResults) {
        String message;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_GET_ACCOUNTS:
                message = "need this permission to fill your name automatically.";
                break;
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                message = "need this permission to access files in external storage.";
                break;
            case PERMISSIONS_REQUEST_RECORD_AUDIO:
                message = "need this permission to make an audio message.";
                break;
            case PERMISSIONS_REQUEST_CAMERA_VISUALIZER:
                message = "need this permission to show camera immersive equalizer.";
                break;
            case PERMISSIONS_REQUEST_CAMERA_PHOTO:
                message = "need this permission to take a picture";
                break;
            case PERMISSIONS_REQUEST_CAMERA_VIDEO:
                message = "need this permission to take a video.";
                break;
            default:
                message = "Please agree to request permission next time";
                break;
        }

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(activity, "Thank you!, " + message, Toast.LENGTH_LONG).show();
            ((PermissionActionListener)activity).onPermissionGranted(requestCode);

        } else {
            //Toast.makeText(activity, "Sorry about closing, " + message, Toast.LENGTH_LONG).show();
            ((PermissionActionListener)activity).onPermissionDenied();
        }
        return;
    }

    public static void requestPermission(Activity activity, String permission, int requestCode) {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                //Toast.makeText(activity, "You must agree to this permission to using this functionality", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            ((PermissionActionListener)activity).onPermissionGranted(requestCode);
        }
    }
}
