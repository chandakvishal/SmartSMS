package com.example.vinay.sms.Messaging.Send;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.example.vinay.sms.MainActivity;
import com.example.vinay.sms.Messaging.Display.SentMessageDisplay;
import com.example.vinay.sms.R;
import com.example.vinay.sms.Utilities.DatabaseHandler;
import com.onegravity.contactpicker.contact.Contact;
import com.onegravity.contactpicker.contact.ContactDescription;
import com.onegravity.contactpicker.contact.ContactSortOrder;
import com.onegravity.contactpicker.core.ContactPickerActivity;
import com.onegravity.contactpicker.picture.ContactPictureType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

public class SendMessage extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    private MultiAutoCompleteTextView txtPhoneNumber;

    private static final int REQUEST_CONTACT = 0;

    private final ArrayList<String> contactNameList = new ArrayList<>();

    private final ArrayList<String> contactNumberList = new ArrayList<>();

    private HashMap<String, String> nameToNumberMap = new HashMap<>();

    private List<Contact> mContacts;

    private final sendSms sendSMS = new sendSms();

    private SentMessageDisplay sentMessageDisplay = new SentMessageDisplay();

    private DatabaseHandler db;

    @SuppressWarnings({"ConstantConditions", "deprecation"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sendmessage);

        db = new DatabaseHandler(getApplicationContext());

        txtPhoneNumber = (MultiAutoCompleteTextView) findViewById(R.id.txtPhoneNumber);

        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        final EditText messageText = (EditText) findViewById(R.id.messageSendInbox);

        findViewById(R.id.buttonSendInbox).setOnClickListener(new View.OnClickListener() {
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
                        sendSMS.sendSMS(phoneNumber, messageToSend, getApplicationContext());
                        updateList();
                    }
                }
            }
        });

        findViewById(R.id.crashButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forceCrash(view);
            }
        });

        findViewById(R.id.get_contacts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ContactPickerActivity.class)
                        .putExtra(ContactPickerActivity.EXTRA_THEME, "dark")

                        .putExtra(ContactPickerActivity.EXTRA_CONTACT_BADGE_TYPE,
                                ContactPictureType.ROUND.name())

                        .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION,
                                ContactDescription.ADDRESS.name())

                        .putExtra(ContactPickerActivity.EXTRA_CONTACT_DESCRIPTION_TYPE,
                                ContactsContract.CommonDataKinds.Email.TYPE_WORK)

                        .putExtra(ContactPickerActivity.EXTRA_CONTACT_SORT_ORDER,
                                ContactSortOrder.AUTOMATIC.name());

                startActivityForResult(intent, REQUEST_CONTACT);
            }
        });

        final CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .coordinatorLayout);
        Snackbar snackbar = Snackbar.make(coordinatorLayout, "Successfully Sent the message", Snackbar.LENGTH_LONG);
        final View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(getResources().getColor(R.color.Black));
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(getResources().getColor(R.color.YellowGreen));

        nameToNumberMap = db.getContacts();
        for (Map.Entry<String, String> entry : nameToNumberMap.entrySet()) {
            contactNameList.add(entry.getKey());
            contactNumberList.add(entry.getValue());
        }

        String[] nameValue = contactNameList.toArray(new String[contactNameList.size()]);

        ArrayAdapter adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, nameValue);
        txtPhoneNumber.setAdapter(adapter);
        txtPhoneNumber.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
    }

    public void forceCrash(View view) {
        throw new RuntimeException("This is a crash");
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CONTACT && resultCode == Activity.RESULT_OK &&
                data != null && data.hasExtra(ContactPickerActivity.RESULT_CONTACT_DATA)) {

            // we got a result from the contact picker --> show the picked contacts
            mContacts = (List<Contact>) data.getSerializableExtra(ContactPickerActivity.RESULT_CONTACT_DATA);
            populateContactList(mContacts);
        }
    }

    private void populateContactList(List<Contact> contacts) {
        if (contacts == null || mContacts.isEmpty()) return;

        // we got a result from the contact picker --> show the picked contacts
        String displayName = "";
        for (Contact contact : contacts) {
            displayName = displayName + contact.getDisplayName() + ",";
        }
        Log.d(TAG, "populateContactList: " + displayName);
        txtPhoneNumber.setText(displayName);
    }

    public void updateList() {
        sentMessageDisplay.updateList();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}

