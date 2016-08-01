package com.example.vinay.sms.Messaging.Send;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.example.vinay.sms.Utilities.ContactUtil;
import com.example.vinay.sms.Utilities.DatabaseHandler;

import static com.example.vinay.sms.Constants.DB_Constants.TABLE_SENT;

public class SendSms {

    private final String TAG = this.getClass().getSimpleName();

    private ContactUtil contactUtil;

    //---sends an SMS message to another device---
    public void sendSMS(final String phoneNumber, final String message, final Context ctx) {

        Log.d(TAG, "sendSMS: SENT TO:" + phoneNumber);

        final DatabaseHandler db = new DatabaseHandler(ctx.getApplicationContext());

        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(ctx, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(ctx, 0,
                new Intent(DELIVERED), 0);

        SmsManager sms = SmsManager.getDefault();
        Log.d(TAG, "sendSMS: Phone Number:" + phoneNumber);
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);

        contactUtil = new ContactUtil(ctx);

        //---when the SMS has been sent---
        ctx.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:

                        ContentValues values = new ContentValues();

                        String address = contactUtil.getContactName(phoneNumber);
                        String date = String.valueOf(System.currentTimeMillis());

                        values.put("address", address);//sender name
                        values.put("body", message);
                        values.put("date", date);

                        ctx.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
                        String tempPhoneNumber = phoneNumber.replaceAll("\\D+", "");
                        db.addMessage(tempPhoneNumber, date, message, null, phoneNumber, null, "true", TABLE_SENT);
                        Toast.makeText(ctx, "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(ctx, "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(ctx, "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(ctx, "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(ctx, "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        ctx.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(ctx, "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(ctx, "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));
    }
}
