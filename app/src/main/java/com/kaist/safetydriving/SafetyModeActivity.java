package com.kaist.safetydriving;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SafetyModeActivity extends Activity {
    ImageButton stopButton;
    private IBinder i;
    Button btn119;
    Button btn112;
    LinearLayout emergencyLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        setContentView(R.layout.safetymode);
        btn112 = (Button) findViewById(R.id.btn_call112);
        btn119 = (Button) findViewById(R.id.btn_call119);
        emergencyLayout = (LinearLayout) findViewById(R.id.layout_emergency);

        emergencyLayout.setVisibility(View.VISIBLE);


        Intent bindIntent = new Intent(this, SafetyModeMngrService.class);
        stopButton = (ImageButton) findViewById(R.id.button0);
        stopButton.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v){
                                        ((SafetyModeMngrService.MyBinder)i).safetyModeTurnOff();
                                        unbindService(mConnection);

                                        SafetyModeActivity.this.finish();

                                    }
                                }
        );
        bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);

        btn119.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( Build.VERSION.SDK_INT >= 23 &&
                        ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return  ;
                }
                String number = "tel:" + "119";
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number));
                callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(callIntent);
            }
        });

        btn112.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( Build.VERSION.SDK_INT >= 23 &&
                        ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return  ;
                }
                String number = "tel:" + "112";
                Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number));
                callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(callIntent);
            }
        });
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            i = service;
        }
        public void onServiceDisconnected(ComponentName name) {
            i = null;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "홈키를 이용하세요", Toast.LENGTH_SHORT).show();
    }
}
