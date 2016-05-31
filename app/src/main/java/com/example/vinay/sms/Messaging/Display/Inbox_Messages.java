package com.example.vinay.sms.Messaging.Display;

import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.vinay.sms.Adapter.InboxAdapter;
import com.example.vinay.sms.Helper.InboxTouchHelper;
import com.example.vinay.sms.MainActivity;
import com.example.vinay.sms.Messaging.SMS;
import com.example.vinay.sms.Messaging.Send.SendSms;
import com.example.vinay.sms.R;
import com.example.vinay.sms.Utilities.BackHandledFragment;
import com.example.vinay.sms.Utilities.DatabaseHandler;

import java.util.ArrayList;
import java.util.List;

public class Inbox_Messages extends BackHandledFragment {

    private InboxAdapter mAdapter;

    private String TAG = MainActivity.class.getSimpleName();

    private List<SMS> messagesList = new ArrayList<>();

    private static SMS message;

    SendSms sendSMS = new SendSms();

    @SuppressWarnings({"ConstantConditions", "deprecation"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.inbox_holder, container, false);

        final String destination = message.getSenderNumber();

        RecyclerView recyclerView = (RecyclerView) parentView.findViewById(R.id.recycler_view_for_inbox);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(message.getSenderAddress());

        setHasOptionsMenu(true);

        final EditText messageText = (EditText) parentView.findViewById(R.id.messageSendInbox);

        parentView.findViewById(R.id.buttonSendInbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String messageToSend = String.valueOf(messageText.getText());
                Log.d(TAG, "onClick: " + destination);
                String phoneNumber = destination.replaceAll("\\D+", "");
                phoneNumber = phoneNumber.startsWith("91") ? phoneNumber.substring(2)
                        : phoneNumber.startsWith("0") ? phoneNumber.substring(1) : phoneNumber;
                sendSMS.sendSMS(phoneNumber, messageToSend, getActivity());
            }
        });

        mAdapter = new InboxAdapter(messagesList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        ItemTouchHelper.Callback callback = new InboxTouchHelper(mAdapter, recyclerView);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);

        getMessages();

        return parentView;
    }

    private void getMessages() {

        try {
            DatabaseHandler db = new DatabaseHandler(getActivity().getApplicationContext());
            List<SMS> list;
            String senderAddress = message.getSenderNumber();
            senderAddress = senderAddress.startsWith("+91") ? senderAddress.substring(3) : senderAddress;
            list = db.getAllMessages(senderAddress);
            messagesList.addAll(list);
            Log.d(TAG, "getMessages: " + messagesList.size());
            mAdapter.notifyDataSetChanged();
        } catch (SQLiteException ex) {
            Log.d("SQLiteException", ex.getMessage());
        }
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

    public void setMessageBody(SMS message) {
        Inbox_Messages.message = message;
    }

//    public void sendMessage(String phoneNumber, String message) {
//        Log.d(TAG, "sendSMS: SENT TO:" + phoneNumber);
//
//
//        String SENT = "SMS_SENT";
//        String DELIVERED = "SMS_DELIVERED";
//
//        PendingIntent sentPI = PendingIntent.getBroadcast(getActivity(), 0,
//                new Intent(SENT), 0);
//
//        PendingIntent deliveredPI = PendingIntent.getBroadcast(getActivity(), 0,
//                new Intent(DELIVERED), 0);
//
//        SmsManager sms = SmsManager.getDefault();
//        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
//
//        //---when the SMS has been sent---
//        getActivity().registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context arg0, Intent arg1) {
//                switch (getResultCode()) {
//                    case Activity.RESULT_OK:
//                        Toast.makeText(getActivity().getBaseContext(), "SMS sent",
//                                Toast.LENGTH_SHORT).show();
//                        break;
//                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
//                        Toast.makeText(getActivity().getBaseContext(), "Generic failure",
//                                Toast.LENGTH_SHORT).show();
//                        break;
//                    case SmsManager.RESULT_ERROR_NO_SERVICE:
//                        Toast.makeText(getActivity().getBaseContext(), "No service",
//                                Toast.LENGTH_SHORT).show();
//                        break;
//                    case SmsManager.RESULT_ERROR_NULL_PDU:
//                        Toast.makeText(getActivity().getBaseContext(), "Null PDU",
//                                Toast.LENGTH_SHORT).show();
//                        break;
//                    case SmsManager.RESULT_ERROR_RADIO_OFF:
//                        Toast.makeText(getActivity().getBaseContext(), "Radio off",
//                                Toast.LENGTH_SHORT).show();
//                        break;
//                }
//            }
//        }, new IntentFilter(SENT));
//
//        //---when the SMS has been delivered---
//        getActivity().registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context arg0, Intent arg1) {
//                switch (getResultCode()) {
//                    case Activity.RESULT_OK:
//                        Toast.makeText(getActivity().getBaseContext(), "SMS delivered",
//                                Toast.LENGTH_SHORT).show();
//                        break;
//                    case Activity.RESULT_CANCELED:
//                        Toast.makeText(getActivity().getBaseContext(), "SMS not delivered",
//                                Toast.LENGTH_SHORT).show();
//                        break;
//                }
//            }
//        }, new IntentFilter(DELIVERED));
//    }
}
