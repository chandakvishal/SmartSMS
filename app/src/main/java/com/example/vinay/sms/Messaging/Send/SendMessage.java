package com.example.vinay.sms.Messaging.Send;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.vinay.sms.MainActivity;
import com.example.vinay.sms.Messaging.Display.SmsDisplayFragment;
import com.example.vinay.sms.R;
import com.example.vinay.sms.Utilities.BackHandledFragment;
import com.example.vinay.sms.Utilities.DatabaseHandler;

import java.util.ArrayList;
import java.util.HashMap;

public class SendMessage extends BackHandledFragment {

    private static final String TABLE_SENT = "_SENT";
    private String TAG = MainActivity.class.getSimpleName();

    private MultiAutoCompleteTextView txtPhoneNumber;

    private ArrayList<String> contactNameList = new ArrayList<>();

    private ArrayList<String> contactNumberList = new ArrayList<>();

    private HashMap<String, String> nameToNumberMap = new HashMap<>();

    @SuppressWarnings({"ConstantConditions", "deprecation"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View parentView = inflater.inflate(R.layout.sendmessage, container, false);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setHasOptionsMenu(true);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Send Message");

        txtPhoneNumber = (MultiAutoCompleteTextView) parentView.findViewById(R.id.txtPhoneNumber);

        final EditText messageText = (EditText) parentView.findViewById(R.id.messageSendInbox);

        parentView.findViewById(R.id.buttonSendInbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String messageToSend = String.valueOf(messageText.getText());
                String phoneNumberObtained = txtPhoneNumber.getText().toString() + ",";
                String[] temp = phoneNumberObtained.split(",");
                for (String dest : temp) {
                    dest = dest.trim();
                    if (dest.length() > 0) {
                        String phoneNumber = nameToNumberMap.get(dest);
                        if (phoneNumber == null) {
                            phoneNumber = dest;
                        }
                        phoneNumber = phoneNumber.replaceAll("\\D+","");
                        sendSMS(phoneNumber, messageToSend);
                    }
                }
            }
        });

        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) parentView.findViewById(R.id
                .coordinatorLayout);
        Snackbar snackbar = Snackbar.make(coordinatorLayout, "Successfully Sent the message", Snackbar.LENGTH_LONG);
        final View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(getResources().getColor(R.color.Black));
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(getResources().getColor(R.color.YellowGreen));

        ContentResolver cr1 = getActivity().getContentResolver();

        Cursor managedCursor = cr1.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        assert managedCursor != null;
        if (managedCursor.moveToFirst()) {
            String contactName, contactNumber;

            int nameColumn = managedCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int phoneColumn = managedCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            do {
                //Get the field values
                contactName = managedCursor.getString(nameColumn);
                contactNumber = managedCursor.getString(phoneColumn);
                //noinspection StringEquality
                if ((contactName != " " || contactName != null) && (contactNumber != " " || contactNumber != null)) {
                    contactNameList.add(contactName);
                    contactNumberList.add(contactNumber);
                    nameToNumberMap.put(contactName, contactNumber);
                }
            } while (managedCursor.moveToNext());

            managedCursor.close();

            String[] nameValue = contactNameList.toArray(new String[contactNameList.size()]);

            ArrayAdapter adapter = new ArrayAdapter<>(
                    getActivity(), android.R.layout.simple_list_item_1, nameValue);
            txtPhoneNumber.setAdapter(adapter);
            txtPhoneNumber.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        }
        return parentView;
    }

    //---sends an SMS message to another device---
    private void sendSMS(final String phoneNumber, final String message) {

        Log.d(TAG, "sendSMS: SENT TO:" + phoneNumber);

        final DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());

        String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";

        PendingIntent sentPI = PendingIntent.getBroadcast(getActivity(), 0,
                new Intent(SENT), 0);

        PendingIntent deliveredPI = PendingIntent.getBroadcast(getActivity(), 0,
                new Intent(DELIVERED), 0);

        SmsManager sms = SmsManager.getDefault();
        Log.d(TAG, "sendSMS: Phone Number:" + phoneNumber);
        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);

        //---when the SMS has been sent---
        getActivity().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:

                        Log.d(TAG, "sendSMS: SENT TO OK:" + phoneNumber);

                        ContentValues values = new ContentValues();

                        String date = String.valueOf(System.currentTimeMillis());

                        values.put("address", phoneNumber);//sender name
                        values.put("body", message);
                        values.put("date", date);

                        getActivity().getContentResolver().insert(Uri.parse("content://sms/sent"), values);
                        String tempPhoneNumber = phoneNumber.replaceAll("\\D+","");
                        db.addUser(tempPhoneNumber, date, message, null, phoneNumber, null, "true", TABLE_SENT);
                        Toast.makeText(getActivity().getBaseContext(), "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(getActivity().getBaseContext(), "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(getActivity().getBaseContext(), "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(getActivity().getBaseContext(), "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(getActivity().getBaseContext(), "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        //---when the SMS has been delivered---
        getActivity().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Log.d(TAG, "sendSMS: SENT TO OHK:" + phoneNumber);

                        Toast.makeText(getActivity().getBaseContext(), "SMS delivered",
                                Toast.LENGTH_SHORT).show();



                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(getActivity().getBaseContext(), "SMS not delivered",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));
    }

    @Override
    public boolean onBackPressed() {
        ((MainActivity) getActivity()).changeFragment(new SmsDisplayFragment(), "home", R.anim.enter_anim, R.anim.exit_anim);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

