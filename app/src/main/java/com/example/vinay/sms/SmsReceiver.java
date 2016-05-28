package com.example.vinay.sms;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.ArrayList;

public class SmsReceiver extends BroadcastReceiver {

    private String TAG = SmsReceiver.class.getSimpleName();

    private ArrayList<SMS> updateList = new ArrayList<>();

    private static SmsDisplayFragment smsDisplayFragment = null;

    public SmsReceiver() {
        super();
    }

    public SmsReceiver(SmsDisplayFragment smsDisplayFragment) {
        this.smsDisplayFragment = smsDisplayFragment;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "onReceive: SMS RECEIVER");

        // prepare intent which is triggered if the
        // notification is selected

        Intent notificationIntent = new Intent(context, SmsDisplayFragment.class);

        // use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);

        Uri soundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            //---get the SMS message passed in---
            SmsMessage[] msgs;
            String msg_from;
            if (bundle != null) {
                //---retrieve the SMS message received---
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    assert pdus != null;
                    msgs = new SmsMessage[pdus.length];
                    for (int i = 0; i < msgs.length; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        msg_from = msgs[i].getOriginatingAddress();
                        String msgBody = msgs[i].getMessageBody();
                        String time = String.valueOf(msgs[i].getTimestampMillis());
                        String read = String.valueOf(msgs[i].getStatusOnIcc());

                        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + msg_from));
                        PendingIntent callPendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), callIntent, 0);

                        Log.d(TAG, "onReceive: " + msg_from + "::" + msgBody);

                        SMS m = new SMS(msg_from, time, msgBody, "1", msg_from, read);

                        String address = smsDisplayFragment.getSenderNumber(msg_from);

                        // build notification
                        // the addAction re-use the same intent to keep the example short
                        Notification n = new Notification.Builder(context)
                                .setContentTitle(address)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentIntent(pIntent)
                                .setContentText(msgBody.length() < 9 ? msgBody : msgBody.substring(0, 9) + "...")
                                .setAutoCancel(true)
                                .setStyle(new Notification.BigTextStyle().bigText(msgBody))
                                .setSound(soundUri)
                                .addAction(R.drawable.ic_call, "Call", callPendingIntent).build();

                        NotificationManager notificationManager =
                                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        notificationManager.notify(0, n);

                        // hide the notification after its selected
                        n.flags |= Notification.FLAG_AUTO_CANCEL;

                        updateList.add(m);
                    }
                } catch (Exception e) {
                    Log.d("Exception caught", e.getMessage());
                }

                smsDisplayFragment.updateList(updateList);
            }
        }
    }
}
