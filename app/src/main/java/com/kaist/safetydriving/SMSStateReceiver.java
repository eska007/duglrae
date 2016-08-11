package com.kaist.safetydriving;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.kaist.safetydriving.db.DatabaseHelper;

import java.util.Calendar;

public class SMSStateReceiver extends BroadcastReceiver
{

	public void sendSMS(Context context, String phoneNumber, String message) {
		SmsManager sms = SmsManager.getDefault();
		PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent("SMS_SENT"), 0);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent("SMS_DELIVERED"), 0);
		if(message.equals("")){
			message="[Duglae] I'm driving now. Please call me later.";
		}
		sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
	}

	public void onReceive(Context context, Intent intent)
	{ 
	    String MSG_TYPE=intent.getAction();
	    
	    
	     if(MSG_TYPE.equals("android.provider.Telephony.SMS_RECEIVED"))
	    {
			 Toast toast;
	
		    Bundle bundle = intent.getExtras();
		    Object messages[] = (Object[]) bundle.get("pdus");
		    SmsMessage smsMessage[] = new SmsMessage[messages.length];
		    for (int n = 0; n < messages.length; n++) 
		    {
		        smsMessage[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
		    }
		    
		    // show first message
		      Log.e(""," smsMessage[0].getDisplayOriginatingAddress()=="+smsMessage[0].getDisplayOriginatingAddress()+"  smsMessage[0].getOriginatingAddress()=="+smsMessage[0].getOriginatingAddress()+"  smsMessage[0].getServiceCenterAddress()=="+smsMessage[0].getServiceCenterAddress());
		      final SharedPreferences mpref =context.getSharedPreferences("BLOCK",context.MODE_PRIVATE);
		      String str=mpref.getString("phonesms","Phone");
		      Log.e("phone no==",""+smsMessage[0].getOriginatingAddress());

			SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
			if ( mPrefs.getBoolean("block_call", true) == true ||
					mPrefs.getBoolean("auto_block", true) == true) {
				Log.e("block_call==",""+smsMessage[0].getOriginatingAddress());

					Calendar cal = Calendar.getInstance();
					String mTime = String.format("%02d-%02d-%d %02d:%02d", cal.get(Calendar.MONTH) + 1,
							cal.get(Calendar.DAY_OF_MONTH),
							cal.get(Calendar.YEAR),
							cal.get(Calendar.HOUR_OF_DAY),
							cal.get(Calendar.MINUTE));
					DatabaseHelper db = new DatabaseHelper(context);
					db.newHistoryData(DatabaseHelper.Fields.TYPE_GENERAL, smsMessage[0].getOriginatingAddress(), "SMS", mTime);

				if ( mPrefs.getBoolean("response", false) == true ) {
					String message = mPrefs.getString("response_msg", "[Duglae] I'm driving now. Please call me later.");

					sendSMS(context, smsMessage[0].getOriginatingAddress(), message);
				}
					toast = Toast.makeText(context, "(SMS 수신) 운전중이니 안전운행 하세요!!", Toast.LENGTH_SHORT);
				 	int x =0;
				int y=0;
				toast.setGravity(Gravity.TOP,x,y);
					toast.show();
			}
	    }
	    else if(MSG_TYPE.equals("android.provider.Telephony.SEND_SMS"))
	    {
	    }
	    else
	    {
	        Toast toast = Toast.makeText(context,"SIN ELSE: "+MSG_TYPE , Toast.LENGTH_LONG);
	        toast.show();
	        for(int i=0;i<8;i++)
	        {
	            System.out.println("Blocking SMS **********************");
	        }
	    }
	}
}