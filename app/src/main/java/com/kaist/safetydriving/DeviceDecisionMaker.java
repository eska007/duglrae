package com.kaist.safetydriving;

import com.kaist.safetydriving.Interface.DeviceExternalCheckerListener;

/**
 * Created by kjwook on 2016. 8. 10..
 */
public class DeviceDecisionMaker implements DeviceExternalCheckerListener {

    public SafetyModeMngrService safetyModeMngrService;

    private String device;
    private String value;

    public void init (String device, String value) {

        this.device = device;
        this.value = value;
    }

    private void startSafetyMode() {
        safetyModeMngrService.turnOnSafetyDrivingMode();
    }


    @Override
    public void onMessageReceived(String device, String value) {
        if (this.device.equals(device) && this.value.equals(value)) {
            startSafetyMode();
        }
    }
}

