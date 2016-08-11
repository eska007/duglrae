package com.kaist.safetydriving;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;
import com.kaist.safetydriving.db.DatabaseHelper;

import java.lang.reflect.Method;
import java.util.Calendar;

/**
 * Created by user on 2016-08-10.
 */
public class PluginCallController implements TargetController {
    // To manage phone calling
    private TelephonyManager mTelephonyManager;
    private MyPhoneStateListener mPhoneListener;
    private final Context mContext;

    public PluginCallController(Context context) {
        this.mContext = context;
        mPhoneListener = new MyPhoneStateListener();
        mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public void registerCallListener() {
        mTelephonyManager.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    public void unregisterCallListener() {
        mTelephonyManager.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
    }

    public class MyPhoneStateListener extends PhoneStateListener {
        private ITelephony telephonyService;

        @Override
        public void onCallStateChanged(int state, String incomingNumber){
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                TelephonyManager telephony = (TelephonyManager) mContext.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                try {
                    Class c = Class.forName(telephony.getClass().getName());
                    Method m = c.getDeclaredMethod("getITelephony");
                    m.setAccessible(true);
                    telephonyService = (ITelephony) m.invoke(telephony);
                    telephonyService.silenceRinger();
                    telephonyService.endCall();

                    // Add to history
                    Calendar cal = Calendar.getInstance();
                    String mTime = String.format("%02d-%02d-%d %02d:%02d",	cal.get(Calendar.MONTH)+1,
                            cal.get(Calendar.DAY_OF_MONTH),
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE));
                    DatabaseHelper db = new DatabaseHelper(mContext.getApplicationContext());
                    db.newHistoryData(DatabaseHelper.Fields.TYPE_GENERAL, incomingNumber, "Calling", mTime);

                    // Auto Respond SMS
                    SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
                    if ( mPrefs.getBoolean("response", false) == true ) {
                        String message = mPrefs.getString("response_msg", "[Duglae] I'm driving now. Please call me later.");

                        sendSMS(incomingNumber, message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        PendingIntent sentPI = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, new Intent("SMS_SENT"), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, new Intent("SMS_DELIVERED"), 0);
        if(message.equals("")){
            message="[Duglae] I'm driving now. Please call me later.";
        }
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }
}
