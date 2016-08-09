package com.kaist.safetydriving;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class ExceptionMonitor extends Exception implements Thread.UncaughtExceptionHandler {
    private Activity mActivity;
/*    private Context mContext;
    private ContentResolver mContentResolver;
    private Thread.UncaughtExceptionHandler mDefaultUncaughtExceptionHandler;*/

    public ExceptionMonitor (Activity activity) {
        this("Duglrae Exception");
        mActivity = activity;
    }

    public ExceptionMonitor (String message) {
        super(message);
    }

/*    public ExceptionMonitor (Context context , ContentResolver contentResolver , Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.mContext = context;
        this.mContentResolver = contentResolver;
        this.mDefaultUncaughtExceptionHandler = uncaughtExceptionHandler;
    }*/

    @Override
    public void uncaughtException (Thread _thread, Throwable _throwable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);

        _throwable.printStackTrace (printWriter);
        String stacktrace = result.toString();
        printWriter.close();

        Log.e("Duglrae", "[Exception] " + stacktrace);

        //mDefaultUncaughtExceptionHandler.uncaughtException(_thread, _throwable);

        Intent intent = new Intent(mActivity, MainActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                mActivity.getBaseContext(), 0, intent, intent.getFlags());

        //Following code will restart your application after 2 seconds
        AlarmManager mgr = (AlarmManager) mActivity.getBaseContext()
                .getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                pendingIntent);

        //This will finish your activity manually
        mActivity.finish();

        //This will stop your application and take out from it.
        System.exit(2);

        /*
        Intent launchIntent = new Intent(mContext.getIntent());
        PendingIntent pending = PendingIntent.getActivity(CSApplication.getContext(), 0,
                launchIntent, activity().getIntent().getFlags());
        getAlarmManager().set(AlarmManager.RTC, System.currentTimeMillis() + 2000, pending);
        defaultHandler.uncaughtException(thread, ex);
*/

    }

}
