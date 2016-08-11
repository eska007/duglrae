package com.kaist.safetydriving;

import android.content.Context;

import com.kaist.safetydriving.db.DatabaseHelper;

public class DeviceExternalChecker {

    private Context mContext;
    private String value;

    public DeviceExternalChecker(Context context, String device) {
        mContext = context;
        value = getDeviceConditionValue(device);
        new DeviceDecisionMaker().init(device, value);
    }

    public String getDeviceConditionValue(String device) {
        DatabaseHelper db = new DatabaseHelper(mContext.getApplicationContext());

        return db.getConditionByDevice(device);
    }

}

