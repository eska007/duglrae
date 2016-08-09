package com.kaist.safetydriving;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;
import java.util.Calendar;
import android.support.v4.content.LocalBroadcastManager;

public class SafetyDrivingService extends Service implements Runnable, LocationListener {

    private static final String TAG = "SafetyDrivingService";
    // 시작 ID
    private int mStartId;
    // 서비스에 대한 스레드에 연결된 Handler. 타이머 이용한 반복 처리시 사용
    private Handler mHandler;
    // 서비스 동작 여부 flag
    private boolean mIsRunning;
    // GPS 모듈 사용 여부 flag
    private boolean mIsGPSRunning;
    // SafetyDriving 모드 여부 flag
    private boolean mIsSafetyDriving;
    // Dismiss 여부
    private boolean mIsDismiss;

    private LocationManager mLocationManager;

    // To manage phone calling
    private TelephonyManager mTelephonyManager;
    private MyPhoneStateListener mPhoneListener;

    // 타이머 설정
    private static final int TIMER_PERIOD = 5 * 1000;
    private static final float VELOCITY_FOR_DRIVING_MODE = 15;

    private QDNotification mNote;

    private Context mContext;
    public static final String PREFS_NAME = "SoundSetting";

    public boolean getIsRunning() {
        return mIsRunning;
    }
    // 서비스를 생성할 때 호출
    public void onCreate() {
        Log.d(TAG, "onCreate : Service Creaeted");
        super.onCreate();
        mContext = this;
        initialize(mContext);
        mHandler = new Handler();
        mIsRunning = false;
        mIsGPSRunning = false;
        mIsDismiss = false;
        mIsSafetyDriving = false;
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mNote = new QDNotification(this);
        mPhoneListener = new MyPhoneStateListener();
        mTelephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
    }

    // 서비스 시작할 때 호출. Background에서의 처리가 시작됨.
    // startId : 서비스 시작 요구 id.stopSelf에서 종료할 때 사용.

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStart : Service startId = " + startId);
        super.onStart(intent, startId);
        mStartId = startId;

        // 동작 중이 아니면 run 메소드를 일정 시간 후에 시작
        if (!mIsRunning) {
            Log.d(TAG, "onStart : mIsRunning = false -> true");
            // this : 서비스 처리의 본체인 run 메소드.
            // postDelayed : 일정시간마다 메소드 호출
            mHandler.postDelayed(this, 1000);
            mIsRunning = true;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // 서비스 종료 시 호출
    public void onDestroy() {
        // onDestroy가 호출되어 서비스가 종료되어도
        // postDelayed는 바로 정지되지 않고 다음 번 run 메소드를 호출
        Log.d(TAG, "onDestroy()");
        mIsRunning = false;
        mLocationManager.removeUpdates(this);
        mIsGPSRunning = false;
    }

    // 서비스 처리
    public void run() {
        Log.d(TAG, "run()");

        // Preference Check
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Log.d(TAG, "run() auto_block= " + mPrefs.getBoolean("auto_block", false) +  "mIsgpSRunning = "+  mIsGPSRunning);
        if (mPrefs.getBoolean("auto_block", false) == true && !mIsGPSRunning) {
            mLocationManager.requestLocationUpdates("gps", TIMER_PERIOD, 10, this);//위치정보가 변경되면 알려줌
            mIsGPSRunning = true;
        }
        mHandler.postDelayed(this, TIMER_PERIOD);
    }

    public void turnOnSafetyDrivingMode() {
        mIsSafetyDriving = true;

        // Notification created
        QDNotification mNote = new QDNotification(getApplicationContext());
        mNote.notify_SafetyDrivingModeOn();
        Toast.makeText(this, "안전 운전모드를 시작합니다", Toast.LENGTH_SHORT).show();

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Log.d(TAG, "block_call " + mPrefs.getBoolean("block_call", false));
        if ( mPrefs.getBoolean("block_call", false) == true ||
                mPrefs.getBoolean("auto_block", false) == true) {
            Log.d(TAG, "TrunOnsafetyDrivingMode block_call in");
            setMute(mContext);
            mTelephonyManager.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
        sendMessage(1);
    }

    public void turnOffSafetyDrivingMode() {
        // Notification removed
        QDNotification mNote = new QDNotification(getApplicationContext());
        mNote.removeNote();
        Toast.makeText(this, "안전 운전모드를 종료합니다", Toast.LENGTH_SHORT).show();
        ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(200);
        restoreSetting(mContext);
        mTelephonyManager.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);

        mIsSafetyDriving = false;
        sendMessage(0);
    }

    public void onLocationChanged(Location location) {
        // Check the preference for Auto Blocking
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // To check if the velocity of user is high enough
        float speed = (float) (location.getSpeed() * 3.6);

        if (	mPrefs.getBoolean("auto_block", false) == true	// Preference for Auto blocking
                &&	speed >= VELOCITY_FOR_DRIVING_MODE				// if the velocity is higher than 15km/h
                &&	!mIsSafetyDriving
                ) {
            Intent intent = new Intent(SafetyDrivingService.this, SafetyModeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            turnOnSafetyDrivingMode();
            startActivity(intent);
        }
    }

    public void onProviderDisabled(String provider) {

    }

    public void onProviderEnabled(String provider) {

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    public class MyPhoneStateListener extends PhoneStateListener {
        private ITelephony telephonyService;

        @Override
        public void onCallStateChanged(int state, String incomingNumber){
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                TelephonyManager telephony = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
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
                    DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                    db.newHistoryData(DatabaseHelper.Fields.TYPE_GENERAL, incomingNumber, "Calling", mTime);

                    // Auto Respond SMS
                    SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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
        PendingIntent sentPI = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("SMS_SENT"), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("SMS_DELIVERED"), 0);
        if(message.equals("")){
            message="[Duglae] I'm driving now. Please call me later.";
        }
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
    }

    private final IBinder binder = new MyBinder();
    public class MyBinder extends Binder {

        public boolean safetyModeTurnOn() {
            if (mIsSafetyDriving)
                return false;

            turnOnSafetyDrivingMode();
            return true;
        }

        public void safetyModeTurnOff() {
            turnOffSafetyDrivingMode();
        }
    }
    private void sendMessage(int status) {
        Log.d(TAG, "sendMessage " + status);
        Intent bintent = new Intent("com.kaist.safetydriving");
        bintent.putExtra("status", status);
        LocalBroadcastManager.getInstance(this).sendBroadcast(bintent);
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"onBind()");

        return binder;
    }
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public static void initialize(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        int volume = settings.getInt("volume", -1);
        int mode = settings.getInt("mode", -1);
        if(volume == -1 || mode == -1) {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            volume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
            mode = audioManager.getRingerMode();
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt("volume", volume);
            editor.putInt("mode", mode);
            editor.commit();
            System.out.println("SoundMode Initialize: "+ mode + ", " + volume);
        }
    }

    public static void setMute(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        audioManager.setStreamMute(AudioManager.STREAM_RING, true);
        audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("restore_mode", settings.getInt("mode", -1));
        editor.putInt("restore_volume", settings.getInt("volume", -1));
        editor.commit();
    }

    public static void restoreSetting(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        int volume = settings.getInt("restore_volume", -1);
        int mode = settings.getInt("restore_mode", -1);

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if(mode != -1) {
            audioManager.setRingerMode(mode);
            audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
            audioManager.setStreamMute(AudioManager.STREAM_RING, false);
            audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);

            if(mode == AudioManager.RINGER_MODE_NORMAL || mode == AudioManager.RINGER_MODE_VIBRATE) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("mode", mode);
                editor.commit();
            }
        }
        if(mode == AudioManager.RINGER_MODE_NORMAL && volume > 0)
            audioManager.setStreamVolume(AudioManager.STREAM_RING, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }
}
