package com.example.vinay.sms.Messaging.Receive;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.example.vinay.sms.Messaging.Display.SmsDisplayFragment;
import com.example.vinay.sms.Messaging.SMS;
import com.example.vinay.sms.R;
import com.example.vinay.sms.Utilities.ContactUtil;

import java.util.ArrayList;
import java.util.Random;

public class SmsReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVER = "SMS_RECEIVER";
    private String TAG = SmsReceiver.class.getSimpleName();

    private ArrayList<SMS> updateList = new ArrayList<>();

    private static SmsDisplayFragment smsDisplayFragment = null;

    /**
     * Super Constructor
     */
    public SmsReceiver() {
        super();
    }

    /**
     * @param smsDisplayFragment SmsDisplayFragment object
     */
    public SmsReceiver(SmsDisplayFragment smsDisplayFragment) {
        this.smsDisplayFragment = smsDisplayFragment;
        Log.d(TAG, "onReceiveXXX: SMS RECEIVER");

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "onReceiveXXXXX: SMS RECEIVER");

        ContactUtil contactUtil = new ContactUtil(context);

        // prepare intent which is triggered if the notification is selected
        // use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

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
                        PendingIntent callPendingIntent = PendingIntent.getActivity(context,
                                (int) System.currentTimeMillis(), callIntent, 0);

                        Log.d(TAG, "onReceive: " + msg_from + "::" + msgBody);

                        String address = contactUtil.getSenderName(msg_from);

                        SMS m = new SMS(address, time, msgBody, "1", msg_from, read, "false");

                        // build notification
                        // the addAction re-use the same intent to keep the example short
                        Notification.Builder builder = getNotification(context,
                                address, pIntent, msgBody, soundUri, callPendingIntent);

                        SharedPreferences pref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
                        boolean shouldBeGrouped = pref.getBoolean("groupNotification", false);
                        if (shouldBeGrouped) {
                            builder = setGroup(builder);
                        }

                        Notification n = builder.build();

                        NotificationManager notificationManager =
                                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        notificationManager.notify(getRandomInt(), n);

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

    private int getRandomInt() {
        Random random = new Random();
        return (random.nextInt(9999 - 1000) + 1000);
    }

    /**
     * @param context           Context of received message
     * @param address           Sender Address
     * @param pIntent           PendingIntent for content
     * @param msgBody           Message Body of the received Message
     * @param soundUri          Notification Sound to set
     * @param callPendingIntent PendingIntent for Calls
     * @return Notification.Builder Object
     */
    private Notification.Builder getNotification(Context context, String address,
                                                 PendingIntent pIntent, String msgBody,
                                                 Uri soundUri, PendingIntent callPendingIntent) {
        return new Notification.Builder(context)
                .setContentTitle(address)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pIntent)
                .setContentText(msgBody.length() < 20 ? msgBody : msgBody.substring(0, 20) + "...")
                .setAutoCancel(true)
                .setStyle(new Notification.BigTextStyle().bigText(msgBody))
                .setSound(soundUri)
                .setPriority(Notification.PRIORITY_HIGH)
                .addAction(R.drawable.ic_call_white_48dp, "Call", callPendingIntent);
    }

    /**
     * Used to group notifications together
     *
     * @param builder Notification.Builder Object to edit
     * @return Edited Notification.Builder Object
     */
    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    private Notification.Builder setGroup(Notification.Builder builder) {
        return builder.setGroup(SMS_RECEIVER);
    }
}
