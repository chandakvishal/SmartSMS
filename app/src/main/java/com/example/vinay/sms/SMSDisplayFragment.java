package com.example.vinay.sms;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

public class SmsDisplayFragment extends BackHandledFragment {

    SmsAdapter mAdapter;

    private final String TAG = this.getClass().getSimpleName();

    private List<SMS> smsList = new ArrayList<>();

    private LinkedHashSet<SMS> linkedHashSet = new LinkedHashSet<>();
    private LinkedHashSet<SMS> uniquelinkedHashSet = new LinkedHashSet<>();

    HashMap<String, String> contactsStored = new HashMap<>();

    Inbox_Messages inbox_messages = new Inbox_Messages();

    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.sms_display, container, false);

        RecyclerView recyclerView = (RecyclerView) parentView.findViewById(R.id.recycler_view_for_answers);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Inbox");

        setHasOptionsMenu(true);

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
        return false;
    }

    public void getMessages() {
        Uri mSmsinboxQueryUri = Uri.parse("content://sms/inbox");
        Cursor cursor1 = getActivity().getContentResolver().query(mSmsinboxQueryUri, new String[]{"_id", "thread_id", "address", "person", "date", "body", "type"}, null, null, null);
        getActivity().startManagingCursor(cursor1);
        HashSet<String> senderHashSet = new HashSet<>();
        String[] columns = new String[]{"address", "person", "date", "body", "type"};
        assert cursor1 != null;
        if (cursor1.getCount() > 0) {
//            String count = Integer.toString(cursor1.getCount());
            while (cursor1.moveToNext()) {
                String sender = cursor1.getString(cursor1.getColumnIndex(columns[0]));
//                String name = cursor1.getString(cursor1.getColumnIndex(columns[1]));
                String date = cursor1.getString(cursor1.getColumnIndex(columns[2]));
                String msg = cursor1.getString(cursor1.getColumnIndex(columns[3]));
                String type = cursor1.getString(cursor1.getColumnIndex(columns[4]));
                SMS m = new SMS(sender, date, msg, type);
                if (!senderHashSet.contains(sender)) {
                    uniquelinkedHashSet.add(m);
                    senderHashSet.add(sender);
                }
                linkedHashSet.add(m);
            }
            smsList.addAll(uniquelinkedHashSet);
            for (SMS sender : smsList) {
                String senderAddress = sender.getSenderAddress();
                if (senderAddress.matches("^([+,.\\s,0-9]*)([0-9]+)")) {
                    senderAddress = getContactName(senderAddress);
                }
                sender.setSenderAddress(senderAddress);
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    public void updateList(final ArrayList smsMessage) {
        smsList.addAll(smsMessage);
        mAdapter.notifyDataSetChanged();
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
