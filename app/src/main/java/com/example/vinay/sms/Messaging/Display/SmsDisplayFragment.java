package com.example.vinay.sms.Messaging.Display;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.Toast;

import com.example.vinay.sms.Adapter.SmsAdapter;
import com.example.vinay.sms.Helper.SmsTouchHelper;
import com.example.vinay.sms.MainActivity;
import com.example.vinay.sms.R;
import com.example.vinay.sms.Messaging.SMS;
import com.example.vinay.sms.Messaging.Send.SendMessage;
import com.example.vinay.sms.Messaging.Receive.SmsReceiver;
import com.example.vinay.sms.Utilities.BackHandledFragment;
import com.example.vinay.sms.Utilities.ClickListener;
import com.example.vinay.sms.Utilities.DatabaseHandler;
import com.example.vinay.sms.Utilities.DividerItemDecoration;
import com.example.vinay.sms.Utilities.RecyclerTouchListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

public class SmsDisplayFragment extends BackHandledFragment {

    SmsAdapter mAdapter;

    private final String TAG = this.getClass().getSimpleName();

    private static List<SMS> smsList = new ArrayList<>();

    private static LinkedHashSet<SMS> linkedHashSet = new LinkedHashSet<>();

    private static LinkedHashSet<SMS> uniquelinkedHashSet = new LinkedHashSet<>();

    HashMap<String, String> contactsStored = new HashMap<>();

    Inbox_Messages inbox_messages = new Inbox_Messages();

    private Boolean exit = false;

    private static final String TABLE_INBOX = "_INBOX";

    DatabaseHandler db;


    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.sms_display, container, false);

        RecyclerView recyclerView = (RecyclerView) parentView.findViewById(R.id.recycler_view_for_answers);

        //Used for update on new incoming message
        @SuppressWarnings("unused")
        SmsReceiver smsReceiver = new SmsReceiver(this);

        db = new DatabaseHandler(getActivity().getApplicationContext());

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Messaging");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        setHasOptionsMenu(true);

        FloatingActionButton sendMessageFloatingButton = (FloatingActionButton) parentView.findViewById(R.id.sendMessageFloatingButton);

        sendMessageFloatingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                ((MainActivity) getActivity()).changeFragment(new SendMessage(), "home");
            }

        });

        //Floating Action Button Menu Configuration

        mAdapter = new SmsAdapter(getActivity(), smsList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        ItemTouchHelper.Callback callback = new SmsTouchHelper(mAdapter, recyclerView);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);
        storeContacts();
        getMessages();

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                SMS message = smsList.get(position);

                inbox_messages.setMessageBody(message);

                ((MainActivity) getActivity()).changeFragment(new Inbox_Messages(), "home");
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

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

    public void getMessages() {
        Uri mSmsinboxQueryUri = Uri.parse("content://sms/inbox");

        Cursor cursor1 = getActivity().getContentResolver().query(mSmsinboxQueryUri, new String[]{"_id", "thread_id", "address", "person", "date", "body", "type", "read"}, null, null, null);
        //noinspection deprecation
        getActivity().startManagingCursor(cursor1);
        LinkedHashSet<String> senderHashSet = new LinkedHashSet<>();
        if (smsList.size() == 0) {
            String[] columns = new String[]{"address", "person", "date", "body", "type", "read"};
            assert cursor1 != null;
            if (cursor1.getCount() > 0) {
//            String count = Integer.toString(cursor1.getCount());
                while (cursor1.moveToNext()) {
                    String sender = cursor1.getString(cursor1.getColumnIndex(columns[0]));
//                String name = cursor1.getString(cursor1.getColumnIndex(columns[1]));
                    String date = cursor1.getString(cursor1.getColumnIndex(columns[2]));
                    String msg = cursor1.getString(cursor1.getColumnIndex(columns[3]));
                    String type = cursor1.getString(cursor1.getColumnIndex(columns[4]));
                    String read = cursor1.getString(cursor1.getColumnIndex(columns[5]));

                    SMS m = new SMS(sender, date, msg, type, sender, read, "false");
                    if (!senderHashSet.contains(sender)) {
                        uniquelinkedHashSet.add(m);
                        senderHashSet.add(sender);
                    }
                    linkedHashSet.add(m);
                }
                smsList.clear();
                smsList.addAll(uniquelinkedHashSet);
                for (SMS sender : smsList) {
                    String senderAddress = sender.getSenderAddress();
                    sender.setSenderAddress(getSenderNumber(senderAddress));
                }
                mAdapter.notifyDataSetChanged();

                //Background Thread to store all messages in a SQLiteDataBase for search
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                for (SMS eachMessage : linkedHashSet) {
                                    String senderAddress = eachMessage.getSenderAddress();
                                    String date = eachMessage.getDate();
                                    String msg = eachMessage.getMessage();
                                    String senderNumber = eachMessage.getSenderNumber();
                                    String type = eachMessage.getType();
                                    String readStatus = eachMessage.getReadStatus();
                                    String sentStatus = eachMessage.isSentStatus();
                                    db.addUser(senderAddress, date, msg, type, senderNumber, readStatus, sentStatus, TABLE_INBOX);
                                }
                            }
                        }
                        // Starts the thread by calling the run() method in its Runnable
                ).start();
            }
        }
    }

    public String getSenderNumber(String senderAddress) {
        if (senderAddress.matches("^([+,.\\s0-9]*)([0-9]+)")) {
            senderAddress = getContactName(senderAddress);
        }
        return senderAddress;
    }

    public void updateList(final ArrayList<SMS> smsMessage) {
        for (SMS eachMessage : smsMessage) {
            Log.d(TAG, "updateList: SENDER NUMBER" + eachMessage.getReadStatus());
            smsList.add(0, eachMessage);
            ContentValues values = new ContentValues();

            String senderAddress = eachMessage.getSenderAddress();
            String date = eachMessage.getDate();
            String msg = eachMessage.getMessage();
            String senderNumber = eachMessage.getSenderNumber();
            String type = eachMessage.getType();
            String readStatus = eachMessage.getReadStatus();
            String sentStatus = eachMessage.isSentStatus();

            values.put("address", senderNumber);//sender name
            values.put("body", msg);
            values.put("date", date);
            values.put("type", type);

            getActivity().getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
            db.addUser(senderAddress, date, msg, type, senderNumber, readStatus, sentStatus, TABLE_INBOX);
        }
        for (SMS sender : smsList) {
            String senderAddress = sender.getSenderAddress();
            sender.setSenderAddress(getSenderNumber(senderAddress));
        }
        mAdapter.notifyDataSetChanged();
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

//    public static Bitmap loadContactPhoto(ContentResolver cr, long id) {
//        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
//        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
//        if (input == null) {
//            return null;
//        }
//        return BitmapFactory.decodeStream(input);
//    }

    public void storeContacts() {
        Cursor cursor = getActivity().getContentResolver().query
                (ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        assert cursor != null;
        Log.d(TAG, "getContactCount: " + cursor.getCount());
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            Log.d(TAG, "getContactName: " + name);
            String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            Log.d(TAG, "getContactNumber: " + phoneNumber);
            contactsStored.put(phoneNumber, name);
        }
        cursor.close();
    }

    public String getContactName(String number) {

        String name = null;

        // define the columns I want the query to return
        String[] projection = new String[]{
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup._ID};

        // encode the phone number and build the filter URI
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

        // query time
        Cursor cursor = getActivity().getContentResolver().query(contactUri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                //Contact Found
                name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            } else {
                //Contact not found
                name = number;
            }
            cursor.close();
        }
        return name;
    }
}
