package com.kaist.safetydriving;

import android.content.Context;
import android.util.Log;

/**
 * Created by user on 2016-08-10.
 */
public class PluginRegister {

    private final String TAG = "PluginRegister";
    private final Context mContext;
    PlugInInputGPS plugIn;

    public PluginRegister(Context context) {
        this.mContext = context;
        plugIn = new PlugInInputGPS(context);
        plugIn.initLocationService();
    }

    public void initService() {
        plugIn.initLocationService();
    }

    public boolean isServiceRunning() {
        return plugIn.ismIsGPSRunning();
    }

    public void setServiceRunning(boolean running) {
        plugIn.setmIsGPSRunning(running);
    }

    public void cancelService() {
        plugIn.cancelLocationService();
    }

    public void requestService() {
        plugIn.requestLocationService();
        Log.i(TAG, "requestService");
        plugIn.test(mContext);
    }
}
