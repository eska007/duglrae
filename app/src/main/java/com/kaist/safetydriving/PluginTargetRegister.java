package com.kaist.safetydriving;

import android.content.Context;

/**
 * Created by user on 2016-08-10.
 */
public class PluginTargetRegister {
    private final Context mContext;
    private PluginCallController callPlugin;
    private PluginSMSController smsPlugin;


    public PluginTargetRegister(Context context) {
        this.mContext = context;
        callPlugin = new PluginCallController(context);
        smsPlugin = new PluginSMSController(context);
    }

    public void registerTargetListener() {
        callPlugin.registerCallListener();
        smsPlugin.registerSMSStatetListener();
    }

    public void unregisterTargetListener() {
        callPlugin.unregisterCallListener();
        smsPlugin.unregisterSMSStateListener();
    }
}
