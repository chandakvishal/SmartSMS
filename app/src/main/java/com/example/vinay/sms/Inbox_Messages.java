package com.example.vinay.sms;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
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
import java.util.List;

public class Inbox_Messages extends BackHandledFragment {

    private InboxAdapter mAdapter;

//    private String TAG = MainActivity.class.getSimpleName();

    private List<SMS> messagesList = new ArrayList<>();

    private static SMS message;

    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.inbox_holder, container, false);

        RecyclerView recyclerView = (RecyclerView) parentView.findViewById(R.id.recycler_view_for_inbox);

        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Answers");

        setHasOptionsMenu(true);

        mAdapter = new InboxAdapter(messagesList);
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
            String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
            String address = "address=\'" + message.getSenderAddress() + "\'";
            Cursor cur = getActivity().getContentResolver().query(uri, projection, address, null, "date desc");
            assert cur != null;
            if (cur.moveToFirst()) {
                int index_Address = cur.getColumnIndex("address");
//                int index_Person = cur.getColumnIndex("person");
                int index_Date = cur.getColumnIndex("date");
                int index_Body = cur.getColumnIndex("body");
                int index_Type = cur.getColumnIndex("type");
                do {
                    String strAddress = cur.getString(index_Address);
//                    String intPerson = cur.getString(index_Person);
                    String strbody = cur.getString(index_Body);
                    String longDate = cur.getString(index_Date);
                    String int_Type = cur.getString(index_Type);

                    SMS sms = new SMS(strAddress, strbody, longDate, int_Type);
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
        ((MainActivity) getActivity()).changeFragment(new SmsDisplayFragment(), "home");
        return true;
    }

    public void setMessageBody(SMS message) {
        Inbox_Messages.message = message;
    }
}
