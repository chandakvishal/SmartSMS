package com.example.vinay.sms;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.vinay.sms.Utilities.BackHandledFragment;
import com.example.vinay.sms.Utilities.DividerItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class Inbox_Messages extends BackHandledFragment {

    private InboxAdapter mAdapter;

    private String TAG = MainActivity.class.getSimpleName();

    private List<SMS> messagesList = new ArrayList<>();

    private static SMS message;

    private Snackbar snackbar;

    @SuppressWarnings({"ConstantConditions", "deprecation"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.inbox_holder, container, false);

        final String destination = message.getSenderNumber();

        RecyclerView recyclerView = (RecyclerView) parentView.findViewById(R.id.recycler_view_for_inbox);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(destination);

        setHasOptionsMenu(true);

        //Snackbar Settings Initially
        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) parentView.findViewById(R.id
                .coordinatorLayout);
        snackbar = Snackbar.make(coordinatorLayout, "Successfully Sent the message", Snackbar.LENGTH_LONG);
        final View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(getResources().getColor(R.color.Black));
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(getResources().getColor(R.color.YellowGreen));

        final EditText messageText = (EditText) parentView.findViewById(R.id.messageSendInbox);

        parentView.findViewById(R.id.buttonSendInbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String messageToSend = String.valueOf(messageText.getText());
                sendMessage(destination, messageToSend);
            }
        });

        mAdapter = new InboxAdapter(getActivity(), messagesList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        ItemTouchHelper.Callback callback = new InboxTouchHelper(mAdapter, recyclerView);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);

        getMessages();

        return parentView;
    }

    private void getMessages() {

        final String SMS_URI_INBOX = "content://sms/inbox";
        try {
            Uri uri = Uri.parse(SMS_URI_INBOX);
            String[] projection = new String[]{"_id", "address", "person", "body", "date", "type", "read"};
            String address = "address=\'" + message.getSenderNumber() + "\'";
            Log.d(TAG, "getMessages: " + address);
            Cursor cur = getActivity().getContentResolver().query(uri, projection, address, null, "date desc");
            assert cur != null;
            if (cur.moveToFirst()) {
                int index_Address = cur.getColumnIndex("address");
//                int index_Person = cur.getColumnIndex("person");
                int index_Date = cur.getColumnIndex("date");
                int index_Body = cur.getColumnIndex("body");
                int index_Type = cur.getColumnIndex("type");
                int index_Read = cur.getColumnIndex("read");
                do {
                    String strAddress = cur.getString(index_Address);
//                    String intPerson = cur.getString(index_Person);
                    String strbody = cur.getString(index_Body);
                    String longDate = cur.getString(index_Date);
                    String int_Type = cur.getString(index_Type);
                    String read = cur.getString(index_Read);

                    SMS sms = new SMS(strAddress, longDate, strbody, int_Type, strAddress, read);
                    messagesList.add(sms);
                } while (cur.moveToNext());
                mAdapter.notifyDataSetChanged();
                if (!cur.isClosed()) {
                    cur.close();
                }
            }
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

    public void sendMessage(String destination, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            Log.d("TAG", "sendMessage: MESSAGE: " + message);
            ArrayList<String> messageParts = smsManager.divideMessage(message);
            Log.d("TAG", "sendMessage: SIZE: " + messageParts.size());
            smsManager.sendMultipartTextMessage(destination, null, messageParts, null, null);
            snackbar.show();
        } catch (Exception e) {
            snackbar.setText("SMS failed, please try again.").show();
            e.printStackTrace();
        }
    }
}
