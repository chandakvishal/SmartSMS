package com.example.vinay.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.util.ArrayList;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    public static final String SMS_BUNDLE = "pdus";

    private SMS m;

    private ArrayList<SMS> updateList = new ArrayList<>();

    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            assert sms != null;
            for (Object sm : sms) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sm);

                String address = smsMessage.getOriginatingAddress();
                String smsBody = smsMessage.getMessageBody();
                String time = String.valueOf(smsMessage.getTimestampMillis());

                m = new SMS(address, time, smsBody, "1", address);
                updateList.add(m);
            }
            Toast.makeText(context, m.getSenderAddress(), Toast.LENGTH_SHORT).show();

            //this will update the UI with message
            SmsDisplayFragment instMainActivity = new SmsDisplayFragment();

            instMainActivity.updateList(updateList);
        }
    }
}