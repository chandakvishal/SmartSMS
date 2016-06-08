package com.example.vinay.sms.Messaging.Display;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.example.vinay.sms.Adapter.SentTabAdapter;
import com.example.vinay.sms.Helper.SmsTouchHelper;
import com.example.vinay.sms.Messaging.SMS;
import com.example.vinay.sms.Messaging.Send.GetMessages;
import com.example.vinay.sms.R;
import com.example.vinay.sms.Utilities.BackHandledFragment;
import com.example.vinay.sms.Utilities.ClickListener;
import com.example.vinay.sms.Utilities.DividerItemDecoration;
import com.example.vinay.sms.Utilities.RecyclerTouchListener;

import static com.example.vinay.sms.Constants.DB_Constants.TABLE_SENT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

public class SentMessageDisplay extends BackHandledFragment {

    private static SentTabAdapter mAdapter;

    private static List<SMS> smsList = new ArrayList<>();

    private static final LinkedHashSet<SMS> linkedHashSet = new LinkedHashSet<>();

    private static final LinkedHashSet<SMS> uniquelinkedHashSet = new LinkedHashSet<>();

    private final Inbox_Messages inbox_messages = new Inbox_Messages();

    private Boolean exit = false;

    GetMessages getMessagesObject;

    private static final HashMap<String, Integer> countOfMessages = new HashMap<>();

    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View parentView = inflater.inflate(R.layout.sms_display, container, false);

        RecyclerView recyclerView = (RecyclerView) parentView.findViewById(R.id.recycler_view_for_answers);

        getMessagesObject = new GetMessages(getActivity());

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Messaging");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        setHasOptionsMenu(true);

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(parentView.getWindowToken(), 0);

        //Floating Action Button Menu Configuration

        mAdapter = new SentTabAdapter(getActivity(), smsList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        ItemTouchHelper.Callback callback = new SmsTouchHelper(mAdapter, recyclerView, ItemTouchHelper.LEFT);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);

        SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean isFirstRun = wmbPreference.getBoolean("FIRSTRUNSENT", true);
        if (isFirstRun) {
            // Code to run once
            SharedPreferences.Editor editor = wmbPreference.edit();
            editor.putBoolean("FIRSTRUNSENT", false);
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
        Uri mSmsinboxQueryUri = Uri.parse("content://sms/sent");

        Cursor cursor1 = getActivity().getContentResolver().query(mSmsinboxQueryUri, new String[]{"_id", "thread_id", "address", "person", "date", "body", "type", "read"}, null, null, null);
        //noinspection deprecation
        getActivity().startManagingCursor(cursor1);
        if (smsList.size() == 0) {
            getMessagesObject.getMessgaes(cursor1, uniquelinkedHashSet, linkedHashSet, countOfMessages, "true", TABLE_SENT);
            smsList.addAll(uniquelinkedHashSet);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void updateList() {
        smsList.clear();
        countOfMessages.clear();
        LinkedHashSet<SMS> uniquelinkedHashSet = new LinkedHashSet<>();
        getMessagesObject.updateList(countOfMessages, uniquelinkedHashSet, TABLE_SENT);
        smsList.addAll(uniquelinkedHashSet);
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
}
