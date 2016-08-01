package com.example.vinay.sms.Messaging.Display;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.vinay.sms.Adapter.SmsAdapter;
import com.example.vinay.sms.Helper.SmsTouchHelper;
import com.example.vinay.sms.Messaging.Receive.SmsReceiver;
import com.example.vinay.sms.Messaging.SMS;
import com.example.vinay.sms.R;
import com.example.vinay.sms.Utilities.BackHandledFragment;
import com.example.vinay.sms.Utilities.ClickListener;
import com.example.vinay.sms.Utilities.DatabaseHandler;
import com.example.vinay.sms.Utilities.DividerItemDecoration;
import com.example.vinay.sms.Utilities.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import static com.example.vinay.sms.Constants.DB_Constants.TABLE_INBOX;

public class SmsDisplayFragment extends BackHandledFragment {

    private final String TAG = this.getClass().getSimpleName();

    private static List<SMS> smsList = new ArrayList<>();
    private static final LinkedHashSet<SMS> linkedHashSet = new LinkedHashSet<>();
    private static final LinkedHashSet<SMS> uniquelinkedHashSet = new LinkedHashSet<>();
    private static final HashMap<String, Integer> countOfMessages = new HashMap<>();
    private final Inbox_Messages inbox_messages = new Inbox_Messages();

    private static DatabaseHandler db;
    private static SmsAdapter mAdapter;

    GetMessages getMessagesObject;

    private Boolean exit = false;

    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View parentView = inflater.inflate(R.layout.sms_display, container, false);

        RecyclerView recyclerView = (RecyclerView) parentView.findViewById(R.id.recycler_view_for_answers);

        getMessagesObject = new GetMessages(getActivity());

        //Used for update on new incoming message
        @SuppressWarnings("unused")
        SmsReceiver smsReceiver = new SmsReceiver(this);

        db = new DatabaseHandler(getActivity().getApplicationContext());

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Messaging");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        setHasOptionsMenu(true);

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(parentView.getWindowToken(), 0);

        mAdapter = new SmsAdapter(getActivity(), smsList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        ItemTouchHelper.Callback callback = new SmsTouchHelper(mAdapter, recyclerView, ItemTouchHelper.RIGHT);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);

        final SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean isFirstRun = wmbPreference.getBoolean("FIRSTRUNINBOX", true);
        if (isFirstRun) {
            // Code to run once
            SharedPreferences.Editor editor = wmbPreference.edit();
            editor.putBoolean("FIRSTRUNINBOX", false);
            editor.apply();
            getMessages();
        } else {
            updateList();
        }

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {

                SMS message = smsList.get(position);
                inbox_messages.setMessageBody(message);
                startActivity(new Intent(getContext(), Inbox_Messages.class));
                getActivity().finish();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        ContentResolver cr1 = getActivity().getContentResolver();

        final Cursor managedCursor = cr1.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        int numOfContacts = wmbPreference.getInt("numOfContacts", 0);

        assert managedCursor != null;
        if (managedCursor.getCount() != numOfContacts) {
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {

                            //TODO: DELETE THE TABLE BEFORE NEW UPDATE
                            Log.d(TAG, "run: " + "Entered Thread for Execution");
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
                                        db.addContacts(contactName, contactNumber);
                                    }
                                } while (managedCursor.moveToNext());
                                SharedPreferences.Editor editor = wmbPreference.edit();
                                editor.putInt("numOfContacts", managedCursor.getCount());
                                editor.apply();
                            }
                            managedCursor.close();
                        }
                    }).start();
        }

        return parentView;
    }

    @Override
    public boolean onBackPressed() {
        if (exit) {
            getActivity().finish(); // finish activity
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);
        }
        return true;
    }

    private void getMessages() {
        Uri mSmsinboxQueryUri = Uri.parse("content://sms/inbox");

        Cursor cursor1 = getActivity().getContentResolver().query(mSmsinboxQueryUri, new String[]{"_id", "thread_id", "address", "person", "date", "body", "type", "read"}, null, null, null);
        //noinspection deprecation
        getActivity().startManagingCursor(cursor1);
        if (smsList.size() == 0) {
            getMessagesObject.getMessgaes(cursor1, uniquelinkedHashSet, linkedHashSet, countOfMessages, "false", TABLE_INBOX);
            smsList.addAll(uniquelinkedHashSet);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void updateList() {
        smsList.clear();
        countOfMessages.clear();
        LinkedHashSet<SMS> uniquelinkedHashSet = new LinkedHashSet<>();
        getMessagesObject.updateList(countOfMessages, uniquelinkedHashSet, TABLE_INBOX);
        smsList.addAll(uniquelinkedHashSet);
        mAdapter.notifyDataSetChanged();
    }

    public void updateList(final ArrayList<SMS> smsMessage) {
        for (SMS eachMessage : smsMessage) {
            Log.d(TAG, "updateList: SENDER NUMBER" + eachMessage.getReadStatus());
            ContentValues values = new ContentValues();

            String senderAddress = eachMessage.getSenderAddress();
            String date = eachMessage.getDate();
            String msg = eachMessage.getMessage();
            String senderNumber = eachMessage.getSenderNumber();
            String type = eachMessage.getType();
            String readStatus = eachMessage.getReadStatus();
            String sentStatus = eachMessage.getSentStatus();

            values.put("address", senderNumber);//sender name
            values.put("body", msg);
            values.put("date", date);
            values.put("type", type);

            getActivity().getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
            db.addMessage(senderAddress, date, msg, type, senderNumber, readStatus, sentStatus, TABLE_INBOX);
        }

        updateList();
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

    public static int getCountOfUnreadMessages(String senderAddress) {
        try {
            return countOfMessages.get(senderAddress);
        } catch (Exception e) {
            return 0;
        }
    }
}
