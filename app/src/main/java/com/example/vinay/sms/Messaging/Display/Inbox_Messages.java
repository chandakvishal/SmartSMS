package com.example.vinay.sms.Messaging.Display;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.example.vinay.sms.Adapter.InboxAdapter;
import com.example.vinay.sms.Helper.InboxTouchHelper;
import com.example.vinay.sms.MainActivity;
import com.example.vinay.sms.Messaging.SMS;
import com.example.vinay.sms.Messaging.Send.SendSms;
import com.example.vinay.sms.R;
import com.example.vinay.sms.Utilities.DatabaseHandler;

import java.util.ArrayList;
import java.util.List;

import static com.example.vinay.sms.Constants.DB_Constants.TABLE_INBOX;

public class Inbox_Messages extends AppCompatActivity {

    private InboxAdapter mAdapter;

    private String TAG = MainActivity.class.getSimpleName();

    private List<SMS> messagesList = new ArrayList<>();

    private static SMS message;

    SendSms sendSMS = new SendSms();

    DatabaseHandler db;

    @SuppressWarnings({"ConstantConditions", "deprecation"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences pref = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        boolean isDark = pref.getBoolean("isDark", true);
        if (isDark) {
            setTheme(R.style.AppThemeDark);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.inbox_holder);

        db = new DatabaseHandler(getApplicationContext());

        final String destination = message.getSenderNumber();

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view_for_inbox);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeAsUpIndicator
                (isDark ? R.drawable.ic_arrow_back_white_48dp : R.drawable.ic_arrow_back_black_48dp);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(message.getSenderAddress());

        final EditText messageText = (EditText) findViewById(R.id.messageSendInbox);

        findViewById(R.id.buttonSendInbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String messageToSend = String.valueOf(messageText.getText());
                Log.d(TAG, "onClick: " + destination);
                String phoneNumber = destination.replaceAll("\\D+", "");
                phoneNumber = phoneNumber.startsWith("91") ? phoneNumber.substring(2)
                        : phoneNumber.startsWith("0") ? phoneNumber.substring(1) : phoneNumber;
                sendSMS.sendSMS(phoneNumber, messageToSend, getApplicationContext());
                messageText.setText("");
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });

        mAdapter = new InboxAdapter(messagesList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        ItemTouchHelper.Callback callback = new InboxTouchHelper(mAdapter, recyclerView);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);

        getMessages();
    }

    private void getMessages() {

        try {
            List<SMS> list;
            String senderAddress = message.getSenderNumber();
            senderAddress = senderAddress.startsWith("+91") ? senderAddress.substring(3) : senderAddress;
            senderAddress = senderAddress.replaceAll(" +", "");
            markMessageRead(this, message.getSenderNumber());
            list = db.getAllMessages(senderAddress);
            messagesList.addAll(list);
            mAdapter.notifyDataSetChanged();
        } catch (SQLiteException ex) {
            Log.d("SQLiteException", ex.getMessage());
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void setMessageBody(SMS message) {
        Inbox_Messages.message = message;
    }

    private void markMessageRead(Context context, String number) {
        Log.d(TAG, "markMessageRead: Marked message read for: " + number);
        db.updateReadStatus(TABLE_INBOX, number);
        Uri uri = Uri.parse("content://sms/inbox");
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        try {

            assert cursor != null;
            while (cursor.moveToNext()) {
                if ((cursor.getString(cursor.getColumnIndex("address")).equals(number))) {
                    String SmsMessageId = cursor.getString(cursor.getColumnIndex("_id"));
                    ContentValues values = new ContentValues();
                    values.put("read", true);
                    context.getContentResolver().update(Uri.parse("content://sms/inbox"), values, "_id=" + SmsMessageId, null);
                    return;
                }
            }
            cursor.close();
        } catch (Exception e) {
            Log.e("Mark Read", "Error in Read: " + e.toString());
        }
    }
}
