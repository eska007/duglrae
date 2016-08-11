package com.kaist.safetydriving;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.kaist.safetydriving.Interface.PermissionActionListener;
import com.kaist.safetydriving.Util.ActivityUtilities;

public class MainActivity extends Activity implements PermissionActionListener {
    private static final String TAG = "MainActivity";
    ImageButton startButton;
    private ComponentName mSafetyDrivingService;
    private IBinder i;
    public int serviceStatus;
    private Activity mActivity;

    @Override
    public void onPermissionDenied() {
        finish();
    }

    @Override
    public void onPermissionGranted(int requestCode) {
        Log.d(TAG, "onPermissionGranted");
        if(requestCode == ActivityUtilities.PERMISSIONS_REQUEST_CALL_PHONE) {
            mSafetyDrivingService = startService(new Intent(this, SafetyModeMngrService.class));
            Intent bindIntent = new Intent(this, SafetyModeMngrService.class);
            Log.d(TAG, "onPermissionGranted start binding");
            bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        ActivityUtilities.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        mActivity = this;
        Intent bintent = new Intent(MainActivity.this, SafetyModeActivity.class);
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); //앱의 title bar (action bar)를 제거
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN); //status bar를 색지정 가능하게 해줌
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION); //navigation bar를 색지정 가능하게 해줌

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS); //system ui 영역 밑까지 UI를 확장하게 해줌
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(getResources().getColor(android.R.color.transparent)); //status bar를 투명하게 해줌
            window.setNavigationBarColor(getResources().getColor(android.R.color.transparent));//status bar를 투명하게 해줌
        }

        ActivityUtilities.requestPermission(this, Manifest.permission.SEND_SMS,
                ActivityUtilities.PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        ActivityUtilities.requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION,
                ActivityUtilities.PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        ActivityUtilities.requestPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION,
                ActivityUtilities.PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        ActivityUtilities.requestPermission(this, Manifest.permission.CALL_PHONE,
                ActivityUtilities.PERMISSIONS_REQUEST_CALL_PHONE);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageService, new IntentFilter("com.kaist.safetydriving"));

        Log.d(TAG, "serviceStatus = " + serviceStatus);
        if (serviceStatus == 1) {
            startActivity(bintent);
        } else {
            setContentView(R.layout.activity_main);

        }

        startButton = (ImageButton) findViewById(R.id.button1);
        startButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(MainActivity.this, SafetyModeActivity.class);
                if ( ((SafetyModeMngrService.MyBinder)i).safetyModeTurnOn() ) {
                    startActivity(intent);
                }
            }
        }
        );

        //Regist the exception monitor
        registExceptionMonitor();
    }

    private void registExceptionMonitor() {

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionMonitor(mActivity));

        //Test code for Exception
        Button exceptionBt = (Button) findViewById(R.id.button4);
        exceptionBt.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               final IllegalStateException e = new IllegalStateException("TEST Exception");
               throw e;
           }
        }
        );
    }

    public void viewHistory(View v) {
        Intent intent = new Intent(MainActivity.this, ViewHistoryActivity.class);
        startActivity(intent);
    }

    public void preference(View v) {
        Intent intent = new Intent(MainActivity.this, UserSettingActivity.class);
        startActivity(intent);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected()");
            i = service;
        }
        public void onServiceDisconnected(ComponentName name) {
            i = null;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    public static final String BROAD_CAST_SERVICE_STATUS = "com.kaist.safetydriving";

    public BroadcastReceiver mMessageService = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            serviceStatus = intent.getIntExtra("status", 0);
            Log.d(TAG, "Got status message = " + serviceStatus);
        }
    };
}
