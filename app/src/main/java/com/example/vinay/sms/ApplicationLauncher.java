package com.example.vinay.sms;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;

import android.os.Handler;
import android.util.Log;

import com.example.vinay.sms.Messaging.Display.GetMessages;
import com.example.vinay.sms.Messaging.SMS;
import com.example.vinay.sms.Utilities.DatabaseHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

import static com.example.vinay.sms.Constants.DB_Constants.TABLE_INBOX;

public class ApplicationLauncher extends AppCompatActivity {

    ArrayList<SMS> smsList = new ArrayList<>();
    int SPLASH_TIME_OUT = 12000;
    GetMessages getMessagesObject;
    private static DatabaseHandler db;
    private static final LinkedHashSet<SMS> linkedHashSet = new LinkedHashSet<>();
    private static final LinkedHashSet<SMS> uniquelinkedHashSet = new LinkedHashSet<>();
    private static final HashMap<String, Integer> countOfMessages = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getMessagesObject = new GetMessages(this);
        db = new DatabaseHandler(getApplicationContext());

        final SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = wmbPreference.getBoolean("FIRSTRUN", true);
        if (isFirstRun) {
            // Code to run once
            SharedPreferences.Editor editor = wmbPreference.edit();
            editor.putBoolean("FIRSTRUN", false);
            editor.apply();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    // This method will be executed once the timer is over
                    // Start your app main activity
                    Intent i = new Intent(ApplicationLauncher.this, MainActivity.class);
                    startActivity(i);

                    // close this activity
                    finish();
                }
            }, SPLASH_TIME_OUT);
            getMessages();
            saveContacts();
        } else {
            Intent i = new Intent(ApplicationLauncher.this, MainActivity.class);
            startActivity(i);
        }
    }

    private void getMessages() {
        Uri mSmsinboxQueryUri = Uri.parse("content://sms/inbox");

        Cursor cursor1 = this.getContentResolver().query(mSmsinboxQueryUri, new String[]{"_id", "thread_id", "address", "person", "date", "body", "type", "read"}, null, null, null);
        //noinspection deprecation
        this.startManagingCursor(cursor1);
        getMessagesObject.getMessgaes(cursor1, uniquelinkedHashSet, linkedHashSet, countOfMessages, "false", TABLE_INBOX);
        smsList.addAll(uniquelinkedHashSet);
    }

    private void saveContacts() {

        ContentResolver cr1 = this.getContentResolver();
        final Cursor managedCursor = cr1.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        assert managedCursor != null;
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {

                        //TODO: DELETE THE TABLE BEFORE NEW UPDATE
                        Log.d("Application Launcher", "run: " + "Entered Thread for Execution");
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
                        }
                        managedCursor.close();
                    }
                }).start();
    }
}
