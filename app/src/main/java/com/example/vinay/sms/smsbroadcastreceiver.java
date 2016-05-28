package com.example.vinay.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    public static final String SMS_BUNDLE = "pdus";

    private SMS m;

    private String TAG = SmsBroadcastReceiver.class.getSimpleName();


    private ArrayList<SMS> updateList = new ArrayList<>();

    public void onReceive(Context context, Intent intent) {

        Toast.makeText(context, "VISHAL", Toast.LENGTH_SHORT).show();

        Log.d(TAG, "onReceive: " + "***BroadcastReceiver***");
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            assert sms != null;
            for (Object sm : sms) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sm);

                String address = smsMessage.getOriginatingAddress();
                String smsBody = smsMessage.getMessageBody();
                String time = String.valueOf(smsMessage.getTimestampMillis());
                String read = String.valueOf(smsMessage.getStatusOnIcc());

                m = new SMS(address, time, smsBody, "1", address, read);
                updateList.add(m);
            }
            Toast.makeText(context, m.getSenderAddress(), Toast.LENGTH_SHORT).show();

            //this will update the UI with message
            SmsDisplayFragment instMainActivity = new SmsDisplayFragment();

            instMainActivity.updateList(updateList);
        }
    }
}