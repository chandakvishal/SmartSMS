package com.example.vinay.sms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class SendMessage extends Activity {

    private String TAG = MainActivity.class.getSimpleName();

    private Snackbar snackbar;

    private MultiAutoCompleteTextView txtPhoneNumber;

    private ArrayList<String> contactNameList = new ArrayList<>();

    private ArrayList<String> contactNumberList = new ArrayList<>();

    private String nameValue[];

    private String numberValue[];

    private HashMap<String, String> nameToNumberMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sendmessage);

        txtPhoneNumber = (MultiAutoCompleteTextView) findViewById(R.id.txtPhoneNumber);

        final EditText messageText = (EditText) findViewById(R.id.messageSendInbox);

        findViewById(R.id.buttonSendInbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String messageToSend = String.valueOf(messageText.getText());

                String[] temp = txtPhoneNumber.getText().toString().split(",");
                for (String dest : temp) {
                    dest = dest.trim();
                    if (dest.length() > 0) {
                        sendSMS(nameToNumberMap.get(dest), messageToSend);
                    }
                }
            }
        });

        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .coordinatorLayout);
        snackbar = Snackbar.make(coordinatorLayout, "Successfully Sent the message", Snackbar.LENGTH_LONG);
        final View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(getResources().getColor(R.color.Black));
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(getResources().getColor(R.color.YellowGreen));

        ContentResolver cr1 = getContentResolver();

        Cursor managedCursor = cr1.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        assert managedCursor != null;
        if (managedCursor.moveToFirst()) {
            String contactName, contactNumber;

            int nameColumn = managedCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int phoneColumn = managedCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            System.out.println("NAME" + nameColumn);
            System.out.println("number is:" + phoneColumn);
            do {
                //Get the field values
                contactName = managedCursor.getString(nameColumn);
                contactNumber = managedCursor.getString(phoneColumn);
                //noinspection StringEquality
                if ((contactName != " " || contactName != null) && (contactNumber != " " || contactNumber != null)) {
                    contactNameList.add(contactName);
                    contactNumberList.add(contactNumber);
                    nameToNumberMap.put(contactName, contactNumber);
                    Log.d(TAG, "onCreate: " + contactName + ":::" + contactName.length());
                    Log.d(TAG, "onCreate: "  + contactNumber);
                }
            } while (managedCursor.moveToNext());

            nameValue = contactNameList.toArray(new String[contactNameList.size()]);
            numberValue = contactNumberList.toArray(new String[contactNameList.size()]);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_list_item_1, nameValue);
            txtPhoneNumber.setAdapter(adapter);
            txtPhoneNumber.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        }
    }

//    public void sendMessage(String destination, String message) {
//        try {
//            SmsManager smsManager = SmsManager.getDefault();
//            Log.d("TAG", "sendMessage: MESSAGE: " + message);
//            ArrayList<String> messageParts = smsManager.divideMessage(message);
//            Log.d("TAG", "sendMessage: SIZE: " + messageParts.size());
//            smsManager.sendMultipartTextMessage(destination, null, messageParts, null, null);
//            snackbar.show();
//        } catch (Exception e) {
//            snackbar.setText("SMS failed, please try again.").show();
//            e.printStackTrace();
//        }
//    }

    //---sends an SMS message to another device---
    private void sendSMS(String phoneNumber, String message)
    {

        Log.d(TAG, "sendSMS: SENT TO:" + phoneNumber);


        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
                new Intent(DELIVERED), 0);

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));
    }
}

