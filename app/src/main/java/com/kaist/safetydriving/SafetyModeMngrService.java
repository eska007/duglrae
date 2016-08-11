package com.kaist.safetydriving;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class SafetyModeMngrService extends Service implements Runnable {

    private static final String TAG = "SafetyDrivingService";
    // 시작 ID
    private int mStartId;
    // 서비스에 대한 스레드에 연결된 Handler. 타이머 이용한 반복 처리시 사용
    private Handler mHandler;
    // 서비스 동작 여부 flag
    private boolean mIsRunning;

    // SafetyDriving 모드 여부 flag
    private static boolean mIsSafetyDriving;
    // Dismiss 여부
    private boolean mIsDismiss;

    private PluginRegister mPlugin;
    private PluginTargetRegister mTargetPlugin;

    // 타이머 설정
    private static final int TIMER_PERIOD = 5 * 1000;
    private static final float VELOCITY_FOR_DRIVING_MODE = 15;

    private QDNotification mNote;

    private Context mContext;
    public static

    final String PREFS_NAME = "SoundSetting";

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
        mIsDismiss = false;
        mIsSafetyDriving = false;

        mPlugin = new PluginRegister(mContext);
        mPlugin.initService();
        mTargetPlugin = new PluginTargetRegister(mContext);

        mNote = new QDNotification(this);

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
//            mHandler.postDelayed(this, 1000);
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
        mPlugin.cancelService();
    }

    // 서비스 처리
    public void run() {
        Log.d(TAG, "run()");

        // Preference Check
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Log.d(TAG, "run() auto_block= " + mPrefs.getBoolean("auto_block", false) + "mIsgpSRunning = " + mPlugin.isServiceRunning());
        if (mPrefs.getBoolean("auto_block", false) == true && !mPlugin.isServiceRunning()) {
            mPlugin.requestService();
        }
        mHandler.postDelayed(this, TIMER_PERIOD);
    }

    public void turnOnSafetyDrivingMode() {
        mIsSafetyDriving = true;

        mNote.notify_SafetyDrivingModeOn();
        Toast.makeText(this, "안전 운전모드를 시작합니다", Toast.LENGTH_SHORT).show();

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Log.d(TAG, "block_call " + mPrefs.getBoolean("block_call", false));
        if ( mPrefs.getBoolean("block_call", false) == true ||
                mPrefs.getBoolean("auto_block", false) == true) {
            Log.d(TAG, "TrunOnsafetyDrivingMode block_call in");
            setMute();
            mTargetPlugin.registerTargetListener();
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
        mTargetPlugin.unregisterTargetListener();

        mIsSafetyDriving = false;
        sendMessage(0);
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
        Log.d(TAG, "onBind()");

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

    public void setMute() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        audioManager.setStreamMute(AudioManager.STREAM_RING, true);
        audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

        SharedPreferences settings = mContext.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("restore_mode", settings.getInt("mode", -1));
        editor.putInt("restore_volume", settings.getInt("volume", -1));
        editor.commit();
    }

    public void restoreSetting(Context context) {
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

    public static boolean ismIsSafetyDriving() {
        return mIsSafetyDriving;
    }
}
