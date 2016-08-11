package com.kaist.safetydriving.Interface;

/**
 * Created by kjwook on 2016. 8. 10..
 */
public interface DeviceExternalCheckerListener {

    void onMessageReceived(String device, String value);
}
