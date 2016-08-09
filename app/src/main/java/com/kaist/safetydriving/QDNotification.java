package com.kaist.safetydriving;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class QDNotification {
    private static final String TAG = "QDNotification";
    private NotificationManager ngr = null;
    private static final int NOTIFY_ME_ID = 1337;
    private Context mContext;

    public QDNotification(Context context) {
        mContext = context;
        ngr = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void notify_SafetyDrivingModeOn() {

        Notification.Builder note = new Notification.Builder(mContext);
        Intent intent = new Intent(mContext, SafetyModeActivity.class);

        PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
        note.setTicker("Blocking mode is turning on...");
        note.setOngoing(true);
        note.setSmallIcon(R.drawable.ic_launcher);
        note.setContentTitle("Safety Driving");
        note.setContentText("Blocking mode is turning on...");
        note.setVibrate(new long[] { 500L, 200L, 200L, 500L });
        note.setContentIntent(pIntent);

        if (Build.VERSION.SDK_INT < 16) {
            ngr.notify(NOTIFY_ME_ID, note.getNotification());
        } else {
            ngr.notify(NOTIFY_ME_ID, note.build());
        }
    }

    public void removeNote() {
        ngr.cancel(NOTIFY_ME_ID);
    }
}
