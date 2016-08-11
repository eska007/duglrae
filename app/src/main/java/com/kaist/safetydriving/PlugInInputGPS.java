package com.kaist.safetydriving;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by user on 2016-08-10.
 */
public class PlugInInputGPS implements LocationListener {
    // GPS 모듈 사용 여부 flag
    private boolean mIsGPSRunning = false;
    private LocationManager mLocationManager;
    private final Context mContext;
    private static final int TIMER_PERIOD = 5 * 1000;
    private static final float VELOCITY_FOR_DRIVING_MODE = 15;

    public PlugInInputGPS(Context context) {
        this.mContext = context;
    }

    public void initLocationService() {
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    public boolean ismIsGPSRunning() {
        return mIsGPSRunning;
    }

    public void setmIsGPSRunning(boolean mIsGPSRunning) {
        this.mIsGPSRunning = mIsGPSRunning;
    }

    public void cancelLocationService() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.removeUpdates((LocationListener) mContext);
        mIsGPSRunning = false;
    }

    public void requestLocationService() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.requestLocationUpdates("gps", TIMER_PERIOD, 10, this);//위치정보가 변경되면 알려줌
        mIsGPSRunning = true;
    }

    void test(Context context) {
        //Intent intent = new Intent(mContext, SafetyModeActivity.class);
//        Intent intent = new Intent();
//        ComponentName cp = new ComponentName("com.kaist.safetydriving", "SafetyModeActivity");
//        intent.setComponent(cp);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent intent = new Intent(context, SafetyModeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        SafetyModeMngrService safetyModeMngrService = new SafetyModeMngrService();
        safetyModeMngrService.turnOnSafetyDrivingMode();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Check the preference for Auto Blocking
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        // To check if the velocity of user is high enough
        float speed = (float) (location.getSpeed() * 3.6);

        if (	mPrefs.getBoolean("auto_block", false) == true	// Preference for Auto blocking
                &&	speed >= VELOCITY_FOR_DRIVING_MODE				// if the velocity is higher than 15km/h
                &&	!SafetyModeMngrService.ismIsSafetyDriving()
                ) {
            //Intent intent = new Intent(mContext, SafetyModeActivity.class);
            Intent intent = new Intent();
            ComponentName cp = new ComponentName("com.kaist.safetydriving", "SafetyModeActivity");
            intent.setComponent(cp);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            SafetyModeMngrService safetyModeMngrService = new SafetyModeMngrService();
            safetyModeMngrService.turnOnSafetyDrivingMode();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
