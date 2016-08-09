package com.kaist.safetydriving.Interface;

/**
 * Created by songhochan on 2016. 1. 25..
 */
public interface PermissionActionListener {
    void onPermissionGranted(int requestCode);
    void onPermissionDenied();
}
