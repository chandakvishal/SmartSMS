package com.example.vinay.sms.Messaging.Send;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import com.example.vinay.sms.MainActivity;
import com.example.vinay.sms.Messaging.Display.SmsDisplayFragment;
import com.example.vinay.sms.R;
import com.example.vinay.sms.Utilities.BackHandledFragment;

import java.util.ArrayList;
import java.util.HashMap;

public class SendMessage extends BackHandledFragment {

//    private String TAG = MainActivity.class.getSimpleName();

    private MultiAutoCompleteTextView txtPhoneNumber;

    private ArrayList<String> contactNameList = new ArrayList<>();

    private ArrayList<String> contactNumberList = new ArrayList<>();

    private HashMap<String, String> nameToNumberMap = new HashMap<>();

    SendSms sendSMS = new SendSms();

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
                        phoneNumber = phoneNumber.replaceAll("\\D+", "");
                        phoneNumber = phoneNumber.startsWith("91") ? phoneNumber.substring(2)
                                : phoneNumber.startsWith("0") ? phoneNumber.substring(1) : phoneNumber;
                        sendSMS.sendSMS(phoneNumber, messageToSend, getActivity());
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

